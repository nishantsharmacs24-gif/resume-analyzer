"""
app.py - Main Flask Application

This is the Python ML Microservice.
Java Spring Boot sends resume text here, 
and this service returns ML analysis results.

Run with: python app.py
Runs on:  http://localhost:5000
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from analyzer.keyword_extractor import extract_keywords, extract_skills_only
from analyzer.similarity_scorer import calculate_similarity, get_score_label
from analyzer.feedback_generator import generate_feedback

# Create Flask app
app = Flask(__name__)
CORS(app)  # Allow cross-origin requests from Java backend


# ──────────────────────────────────────────────────────
# HEALTH CHECK ENDPOINT
# ──────────────────────────────────────────────────────
@app.route('/health', methods=['GET'])
def health():
    """Simple health check — Java can use this to verify Python is running"""
    return jsonify({"status": "OK", "message": "ML Service is running! 🚀"})


# ──────────────────────────────────────────────────────
# MAIN ANALYSIS ENDPOINT
# Called by Java's MLService.java
# ──────────────────────────────────────────────────────
@app.route('/analyze', methods=['POST'])
def analyze():
    """
    Analyze resume against job description.
    
    Receives:
        JSON: { "resume_text": "...", "job_description": "..." }
    
    Returns:
        JSON: {
            match_score, keywords_matched, keywords_missing,
            skills_found, feedback, suggestions, summary
        }
    """
    try:
        data = request.get_json()

        if not data:
            return jsonify({"error": "No data received"}), 400

        resume_text = data.get('resume_text', '')
        job_description = data.get('job_description', '')

        if not resume_text or not job_description:
            return jsonify({"error": "Both resume_text and job_description are required"}), 400

        print(f"\n📄 Analyzing resume... ({len(resume_text)} chars)")
        print(f"📋 Job Description: ({len(job_description)} chars)")

        # ── Step 1: Extract keywords from both texts ──────────
        print("🔍 Extracting keywords...")
        resume_keywords = extract_keywords(resume_text)
        jd_keywords = extract_keywords(job_description)
        resume_skills = extract_skills_only(resume_text)

        # ── Step 2: Calculate similarity score ────────────────
        print("📊 Calculating similarity score...")
        score, matched, missing = calculate_similarity(
            resume_text, job_description,
            resume_keywords, jd_keywords
        )

        score_percent = round(score * 100, 2)
        print(f"✅ Match Score: {score_percent}%")

        # ── Step 3: Generate AI feedback ──────────────────────
        print("🤖 Generating AI feedback...")
        ai_response = generate_feedback(resume_text, job_description, score, missing)

        # ── Step 4: Build and return response ─────────────────
        response = {
            "match_score": score_percent,
            "score_label": get_score_label(score),
            "keywords_matched": ", ".join(matched[:20]),
            "keywords_missing": ", ".join(missing[:20]),
            "skills_found": ", ".join(resume_skills[:20]),
            "feedback": ai_response.get("feedback", ""),
            "suggestions": ai_response.get("suggestions", ""),
            "summary": ai_response.get("summary", "")
        }

        print(f"🎉 Analysis complete! Score: {score_percent}%\n")
        return jsonify(response), 200

    except Exception as e:
        print(f"❌ Error: {str(e)}")
        return jsonify({"error": f"Analysis failed: {str(e)}"}), 500


# ──────────────────────────────────────────────────────
# KEYWORD EXTRACTION ONLY ENDPOINT
# ──────────────────────────────────────────────────────
@app.route('/extract-keywords', methods=['POST'])
def extract():
    """Extract keywords only from text"""
    data = request.get_json()
    text = data.get('text', '')
    keywords = extract_keywords(text)
    return jsonify({"keywords": keywords})


# ──────────────────────────────────────────────────────
# RUN THE FLASK SERVER
# ──────────────────────────────────────────────────────
if __name__ == '__main__':
    print("🚀 Starting ML Service on http://localhost:5000")
    print("📌 Endpoints:")
    print("   GET  /health        - Health check")
    print("   POST /analyze       - Analyze resume vs JD")
    print("   POST /extract-keywords - Extract keywords only\n")
    app.run(host='0.0.0.0', port=5000, debug=True)
