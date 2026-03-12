"""
similarity_scorer.py

Calculates how well a resume matches a job description using:
1. TF-IDF (Term Frequency - Inverse Document Frequency)
   - Converts text to numbers
   - Words that appear more in resume but less commonly overall = more important

2. Cosine Similarity
   - Measures the "angle" between two text vectors
   - 1.0 = perfect match, 0.0 = no match at all
"""

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import numpy as np


def calculate_similarity(resume_text: str, jd_text: str,
                          resume_keywords: list, jd_keywords: list):
    """
    Calculate match score and keyword overlap between resume and job description.
    
    Returns:
        score (float): 0.0 to 1.0 similarity score
        matched (list): Keywords found in both resume and JD
        missing (list): Keywords in JD but missing from resume
    """

    # ── Step 1: TF-IDF Vectorization ────────────────────────
    # Convert both texts to number vectors
    vectorizer = TfidfVectorizer(
        stop_words='english',    # Remove common words: "the", "a", "is"
        max_features=500,        # Consider top 500 most important words
        ngram_range=(1, 2)       # Consider single words AND two-word phrases
    )

    try:
        # Fit on both texts and transform
        tfidf_matrix = vectorizer.fit_transform([resume_text, jd_text])

        # ── Step 2: Cosine Similarity ────────────────────────────
        # Calculate angle between the two text vectors
        # Result is a 2D array: [[similarity_score]]
        similarity_matrix = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])
        score = float(similarity_matrix[0][0])

    except Exception as e:
        print(f"TF-IDF calculation error: {e}")
        score = 0.0

    # ── Step 3: Keyword Matching ─────────────────────────────
    resume_set = set([k.lower() for k in resume_keywords])
    jd_set = set([k.lower() for k in jd_keywords])

    matched = list(resume_set & jd_set)  # Common keywords (intersection)
    missing = list(jd_set - resume_set)  # In JD but not in resume (difference)

    # ── Step 4: Boost score based on keyword overlap ─────────
    if len(jd_set) > 0:
        keyword_match_ratio = len(matched) / len(jd_set)
        # Weighted average: 70% TF-IDF score + 30% keyword match
        final_score = (score * 0.7) + (keyword_match_ratio * 0.3)
    else:
        final_score = score

    # Ensure score is between 0 and 1
    final_score = min(max(final_score, 0.0), 1.0)

    return final_score, matched, missing


def get_score_label(score: float) -> str:
    """Convert numeric score to a human-readable label"""
    percentage = score * 100
    if percentage >= 80:
        return "Excellent Match 🟢"
    elif percentage >= 60:
        return "Good Match 🟡"
    elif percentage >= 40:
        return "Partial Match 🟠"
    else:
        return "Low Match 🔴"
