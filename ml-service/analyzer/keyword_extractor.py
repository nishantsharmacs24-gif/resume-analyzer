"""
keyword_extractor.py

Uses spaCy NLP to extract:
- Named Entities (companies, locations, organizations)
- Technical skills from a predefined skills list
- Education keywords
- Experience level indicators
"""

import spacy
from config import SKILLS_LIST

# Load the English NLP model
# 'en_core_web_sm' = small, fast model
# Run: python -m spacy download en_core_web_sm
try:
    nlp = spacy.load("en_core_web_sm")
except OSError:
    print("⚠️  spaCy model not found. Run: python -m spacy download en_core_web_sm")
    nlp = None


def extract_keywords(text: str) -> list:
    """
    Extract important keywords from resume or job description text.
    
    Args:
        text: Raw text from resume or JD
    
    Returns:
        List of extracted keywords/skills
    """
    if not text or not nlp:
        return []

    text_lower = text.lower()
    keywords = set()

    # ── Step 1: spaCy Named Entity Recognition ──────────────
    # spaCy automatically finds: company names, universities, locations, products
    doc = nlp(text_lower[:10000])  # Limit to 10k chars for speed
    for ent in doc.ents:
        if ent.label_ in ["ORG", "PRODUCT", "GPE", "WORK_OF_ART"]:
            if len(ent.text) > 2:  # Ignore very short entities
                keywords.add(ent.text.strip())

    # ── Step 2: Match against our skills list ───────────────
    for skill in SKILLS_LIST:
        if skill.lower() in text_lower:
            keywords.add(skill.lower())

    # ── Step 3: Extract education keywords ──────────────────
    education_terms = [
        "bachelor", "master", "phd", "b.tech", "m.tech", "b.sc", "m.sc",
        "mba", "bca", "mca", "diploma", "degree", "graduate", "postgraduate"
    ]
    for term in education_terms:
        if term in text_lower:
            keywords.add(term)

    # ── Step 4: Extract experience indicators ───────────────
    import re
    # Match patterns like "3 years", "5+ years"
    exp_pattern = r'\d+\+?\s*years?\s*(of\s+)?(experience|exp)?'
    matches = re.findall(exp_pattern, text_lower)
    if matches:
        keywords.add("experienced")

    return list(keywords)


def extract_skills_only(text: str) -> list:
    """Only return skills from predefined skills list"""
    text_lower = text.lower()
    return [skill for skill in SKILLS_LIST if skill.lower() in text_lower]
