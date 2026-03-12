# config.py
# All configuration values for the ML service

import os

# OpenAI API Key — get yours at https://platform.openai.com/api-keys
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "your_openai_api_key_here")

# Flask server settings
FLASK_PORT = 5000
FLASK_DEBUG = True

# Common tech skills list used for keyword matching
SKILLS_LIST = [
    # Programming Languages
    "python", "java", "javascript", "typescript", "c++", "c#", "ruby", "go",
    "kotlin", "swift", "php", "scala", "rust", "r", "matlab",

    # Frontend
    "react", "angular", "vue", "html", "css", "bootstrap", "tailwind",
    "jquery", "next.js", "redux",

    # Backend
    "spring boot", "django", "flask", "node.js", "express", "fastapi",
    "rest api", "graphql", "microservices",

    # Databases
    "mysql", "postgresql", "mongodb", "redis", "oracle", "sql server",
    "firebase", "elasticsearch",

    # Cloud & DevOps
    "aws", "azure", "google cloud", "docker", "kubernetes", "jenkins",
    "ci/cd", "terraform", "ansible", "linux",

    # AI/ML
    "machine learning", "deep learning", "tensorflow", "pytorch", "scikit-learn",
    "pandas", "numpy", "nlp", "computer vision", "data science",

    # Tools
    "git", "github", "jira", "agile", "scrum", "maven", "gradle",

    # Soft Skills
    "leadership", "communication", "teamwork", "problem solving",
    "critical thinking", "project management", "time management"
]
