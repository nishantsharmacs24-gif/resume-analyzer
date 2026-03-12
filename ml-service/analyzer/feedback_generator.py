"""
feedback_generator.py

Uses OpenAI GPT to generate:
1. Detailed bullet-point feedback
2. Resume improvement suggestions
3. Resume summary
4. Missing skills recommendations
"""

from openai import OpenAI
from config import OPENAI_API_KEY

# Initialize OpenAI client
client = OpenAI(api_key=OPENAI_API_KEY)


def generate_feedback(resume_text: str, job_description: str,
                       score: float, missing_keywords: list) -> dict:
    """
    Generate AI-powered feedback using OpenAI GPT.
    
    Returns:
        dict with: feedback, suggestions, summary
    """

    score_percent = round(score * 100, 1)
    missing_str = ", ".join(missing_keywords[:10]) if missing_keywords else "None"

    # ── PROMPT: Ask GPT to analyze the resume ───────────────
    prompt = f"""
You are an expert HR consultant and resume coach with 15 years of experience.

RESUME TEXT:
{resume_text[:3000]}

JOB DESCRIPTION:
{job_description[:1500]}

MATCH SCORE: {score_percent}%
MISSING KEYWORDS: {missing_str}

Please provide a detailed analysis in the following JSON format ONLY (no extra text):
{{
    "feedback": "3-4 bullet points of specific strengths and weaknesses of this resume for this job",
    "suggestions": "3-4 specific actionable improvement suggestions",
    "summary": "2-3 sentence professional summary of the candidate's profile"
}}

Be specific, actionable, and professional. Focus on:
- Skills alignment with the job
- Experience relevance  
- Missing qualifications
- Resume presentation quality
"""

    try:
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {
                    "role": "system",
                    "content": "You are an expert HR consultant. Always respond with valid JSON only."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            max_tokens=800,
            temperature=0.7
        )

        import json
        raw = response.choices[0].message.content.strip()

        # Clean up response if needed
        if raw.startswith("```"):
            raw = raw.replace("```json", "").replace("```", "").strip()

        result = json.loads(raw)
        return {
            "feedback": result.get("feedback", ""),
            "suggestions": result.get("suggestions", ""),
            "summary": result.get("summary", "")
        }

    except Exception as e:
        print(f"OpenAI error: {e}")
        # Fallback: generate basic feedback without OpenAI
        return generate_basic_feedback(score_percent, missing_keywords)


def generate_basic_feedback(score_percent: float, missing_keywords: list) -> dict:
    """
    Fallback feedback generation without OpenAI.
    Used when OpenAI API is unavailable or quota exceeded.
    """
    missing_str = ", ".join(missing_keywords[:5]) if missing_keywords else "none detected"

    if score_percent >= 70:
        feedback = ("• Strong overall match with the job requirements\n"
                   "• Resume demonstrates relevant technical skills\n"
                   "• Good keyword alignment with job description")
        suggestions = ("• Add more quantifiable achievements (numbers, percentages)\n"
                      "• Consider adding a professional summary at the top\n"
                      f"• Include these missing skills if applicable: {missing_str}")
    elif score_percent >= 40:
        feedback = ("• Moderate match with the job requirements\n"
                   "• Some relevant skills present but gaps exist\n"
                   f"• Missing important keywords: {missing_str}")
        suggestions = ("• Add missing technical skills to your skills section\n"
                      "• Tailor your experience bullet points to match the JD\n"
                      "• Use more industry-specific terminology from the job posting")
    else:
        feedback = ("• Low match with job requirements\n"
                   "• Resume needs significant tailoring for this position\n"
                   f"• Many required skills are missing: {missing_str}")
        suggestions = ("• Rewrite your resume to target this specific role\n"
                      "• Add a skills section with the required technologies\n"
                      "• Consider gaining experience in the missing skill areas")

    summary = (f"Candidate profile shows a {score_percent}% match with the provided "
               f"job description. The resume demonstrates some relevant experience "
               f"but may benefit from targeted improvements.")

    return {
        "feedback": feedback,
        "suggestions": suggestions,
        "summary": summary
    }
