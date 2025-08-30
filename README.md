# 🌐 Social Network Microservices

A **microservices-based social network application** with service discovery, API Gateway routing, real-time messaging, and AI-powered content moderation & recommendations.

---

## 📌 Overview

This project was built as a **team project (3 members)** between **June 2025 - August 2025**.  
It explores **microservices architecture** using **Java Spring Boot** and leverages multiple technologies for scalability, security, and real-time interaction.

---

## 🚀 Tech Stack

- **Backend:** Java, Spring Boot (RESTful Microservices)  
- **Database:** PostgreSQL, Couchbase Server  
- **Messaging & Caching:** Kafka, Redis  
- **Security:** Spring Security, JWT, OAuth2 (Google Login)  
- **Service Discovery & Routing:** Eureka, API Gateway  
- **AI/ML Models:** toxic-BERT (toxic content detection), NSFW detector, BART-MNLI (post recommendation)  
- **Others:** Python (AI integration), Feign Client, WebSockets  

---

## ✨ Features

### 🔐 Authentication & Security
- JWT-based authentication with **username/password** login.  
- **Google social login** via OAuth2.  
- **Refresh token** mechanism for seamless login.  
- Configured **Spring Security** across microservices.  

### 📝 Social Features
- Post management with **CRUD** operations.  
- Reactions and sharing functionality.  
- Manage **personal user information**.  

### 💬 Real-time Messaging
- Enabled chat system using **WebSockets**.  
- Messages encrypted/decrypted with **RSA + AES** via custom cryptography service.  

### 📊 Trending & Recommendations
- Calculate and display **popular trends**.  
- AI-powered **post recommendations** using **BART-MNLI** + **Cosine Similarity**.  

### 🎶 Music Integration
- Spotify API integration for providing music within the app.  

### ⚡ Performance & Scalability
- **Eureka** for service discovery and **API Gateway** for centralized routing.  
- **Redis** caching layer for reducing database query time.  
- **Kafka** & **Feign Client** for inter-service communication and synchronization.  

### 🤖 AI & Moderation
- Integrated **toxic-BERT** model for detecting toxic comments/posts.  
- Added **NSFW detector** to moderate inappropriate content.  

---

## 🧑‍💻 My Role

- Configured **Spring Security, JWT/OAuth2 authentication**, and **Eureka service discovery**.  
- Managed **API Gateway routing** for inter-service communication.  
- Implemented **refresh tokens** for seamless login.  
- Built **cryptography service (RSA & AES)** for secure messaging.  
- Integrated **Redis** caching to improve performance.  
- Enabled **Kafka + Feign Client** for microservice communication.  
- Developed **real-time messaging** via WebSockets.  
- Integrated **AI models** (toxic-BERT, NSFW detector, BART-MNLI).
  
---
