# 🌿 EcoSpot Java - Blog & Statistics Ecosystem

Welcome to the **EcoSpot Java** Blog and Content Management Module. This section of the platform is dedicated to environmental article publishing, community engagement (comments), and advanced analytics, powered by external APIs and AI integrations.

---

## 🤖 Artificial Intelligence & Smart Integrations

### 📄 Article Intelligence
*   **AI SEO Generation**: Automatically generates high-quality SEO titles, meta descriptions, and keywords based on article content to maximize visibility (via OpenRouter AI).
*   **Creative Title Ideas**: Generates 5 catchy, engaging alternative titles for any draft to boost click-through rates.

### 💬 Community & Safety Integrations
*   **Smart Profanity Filter (PurgoMalum)**: Automatically intercepts and censors inappropriate or toxic words in community comments before they are saved to the database.
*   **Face Recognition Login**: Allows users to log in securely using biometric facial recognition via a custom Python Flask microservice integrating `face_recognition` and OpenCV.

---

## 🌐 Integrated External APIs
EcoSpot connects with world-class services to provide a rich, data-driven experience:

| API | Purpose | Implementation |
| :--- | :--- | :--- |
| **OpenRouter** | Large Language Model (LLM) services for all AI features. | `src/main/java/tn/esprit/services/OpenRouterService.java`, `src/main/java/tn/esprit/services/AiSeoService.java` |
| **Unsplash** | Fetching high-resolution environmental imagery for articles. | `src/main/java/tn/esprit/services/UnsplashService.java` |
| **PurgoMalum** | REST API for automated comment profanity filtering. | `src/main/java/tn/esprit/services/CommentService.java` |
| **DiceBear** | Generates procedural pixel-art avatars for users and guest commenters. | `src/main/java/tn/esprit/services/AvatarService.java` |
| **Local Face API** | Custom Python microservice on port 8001 for biometric login. | `src/main/java/tn/esprit/user/FaceLoginController.java`, `face_service.py` |

---

## 🚀 Advanced Module Functionalities

### 📰 Article Management
*   **Smart Slugs**: Automatic URL-friendly slug generation with unique collision prevention.
*   **View Analytics**: Advanced view counter with a **15-minute cooldown** per user to ensure accurate statistics.
*   **Reading Time**: Real-time estimation of article reading time based on content density.
*   **Workflow States**: Complete lifecycle management from `Draft` to `Published` with admin revision notes.
*   **Rich Media**: Seamless integration with Unsplash for instant professional imagery.

### 💬 Advanced Commenting System
*   **Dynamic Guest Identities**: Creative names for anonymous users (e.g., *"Solar Guardian"*, *"Green River"*) generated via ID-based nature algorithms.
*   **Proactive Moderation**: Built-in flagging system with administrative workflow to hide or approve sensitive content.
*   **Automated Censorship**: Bad words are filtered on-the-fly using the PurgoMalum API.
*   **Visual Identity**: Integrated avatar styles linked to user profiles.

### 📂 Taxonomy (Categories & Tags)
*   **Strict Validation**: Naming engine that enforces length, character constraints, and starting requirements.
*   **Auto-Upsert**: Case-insensitive "Create if Missing" logic to prevent duplicate taxonomies.
*   **Relational Integrity**: Transactional deletion that safely unlinks articles before removing categories/tags.
*   **Deep Aggregation**: Real-time article counting and title listing per category or tag.

---

## 📂 Project Architecture & File Responsibilities

### 📰 Content & Blog Management
| File | Responsibility |
| :--- | :--- |
| `src/main/java/tn/esprit/services/BlogService.java` | Core business logic: CRUD, unique slug generation, reading time calculation, and author filtering. |
| `src/main/java/tn/esprit/services/CommentService.java` | Handles comment lifecycles, dynamic guest name generation, PurgoMalum filtering, and moderation. |
| `src/main/java/tn/esprit/services/AvatarService.java` | Integrates with the DiceBear API to generate dynamic procedural pixel-art avatars for profiles and comments. |
| `src/main/java/tn/esprit/services/ReactionService.java` | Manages user likes/dislikes with unique constraints per article. |
| `src/main/java/tn/esprit/services/CategoryService.java` | Validates and manages article categories with recursive deletion protection. |
| `src/main/java/tn/esprit/services/TagService.java` | Case-insensitive tag management and auto-upsert logic. |
| `src/main/java/tn/esprit/blog/BlogDetailController.java` | Main viewer for articles; handles real-time views, reactions, and comment posting. |
| `src/main/java/tn/esprit/blog/NewArticleController.java` | Editor interface with AI-assisted title and SEO generation. |

### 📊 Analytics & Statistics
| File | Responsibility |
| :--- | :--- |
| `src/main/java/tn/esprit/services/StatisticsService.java` | High-performance SQL engine for all charts, KPIs, and unique engagement rate calculations. |
| `src/main/java/tn/esprit/util/StatisticsCollector.java` | Background utility that records granular events (Views, Reactions, Search Terms) to the DB. |
| `src/main/java/tn/esprit/blog/StatisticsController.java` | Primary dashboard for global blog performance (Views over time, Top Content). |
| `src/main/java/tn/esprit/blog/ArticleStatsController.java` | Drilldown analytics for individual articles, showing specific growth and feedback. |

### 🤖 AI & External Integrations
| File | Responsibility & How It Works |
| :--- | :--- |
| `src/main/java/tn/esprit/services/AiSeoService.java` | **SEO & Content Generation:** Uses custom-crafted LLM prompts to analyze an article's raw content. It requests JSON responses from the AI containing optimized SEO metadata and alternative titles. |
| `src/main/java/tn/esprit/services/OpenRouterService.java` | **AI Infrastructure:** Acts as the central asynchronous communication hub for all LLM requests without blocking the JavaFX UI thread. |
| `src/main/java/tn/esprit/services/UnsplashService.java` | **Media Integration:** Connects to the Unsplash API to fetch high-quality, royalty-free environmental images and parses the JSON response to extract image URLs. |
| `face_service.py` | **Face Login Backend:** A standalone Python Flask service providing biometric `/enroll` and `/recognize` endpoints via OpenCV. |

---
*Developed with ❤️ for the Planet.*
