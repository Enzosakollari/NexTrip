# NexTrip

![CI](https://github.com/Enzosakollari/NexTrip/actions/workflows/ci.yml/badge.svg)

NexTrip is a Spring Boot + Thymeleaf travel platform where users search flights, book curated travel packages, and pay securely with Stripe. It also includes an AI travel assistant, multi-role dashboards, Redis caching, Kafka events, and JPA-backed persistence.

## Features
- Flight search (Amadeus API) with caching and optional persistence
- Travel packages with booking and Stripe checkout
- AI assistant for travel suggestions (Hugging Face)
- Business and admin portals with role-based access
- Spring Security for authentication and protected routes
- JWT utilities for token-based auth flows
- Email notifications (Spring Mailer) for verification and updates
- WebSocket support for live interactions
- Redis cache for fast flight offer lookup
- Kafka event publishing for booking events (optional)

## Tech Stack
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" height="60" alt="java logo" />
<img width="12" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" height="60" alt="spring logo" />
<img width="12" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" height="60" alt="mysql logo" />
<img width="12" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg" height="60" alt="redis logo" />
<img width="12" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/apachekafka/apachekafka-original.svg" height="60" alt="kafka logo" />
<img width="12" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/maven/maven-original.svg" height="60" alt="maven logo" />
<img width="12" />
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" height="60" alt="docker logo" />

## Screenshots
Home Page
![Home Page](readmeimages/indexpage.png)

Flight Search
![Flight Search](readmeimages/flight-search.png)

Travel Packages
![Travel Packages](readmeimages/travel-package.png)

AI Chat Assistant
![AI Chat](readmeimages/ai-chat.png)

My Tickets
![My Tickets](readmeimages/tickets.png)

Train Timetable
![Train Timetable](readmeimages/train.png)

## CI & Tests
CI runs on GitHub Actions with a MySQL service and the `ci` Spring profile. Tests are executed with Maven and JaCoCo coverage is generated.

Run tests locally:
```bash
mvn -B clean test
```

Optional: run tests one by one (helpful for debugging):
```powershell
.\run-tests.ps1 -SkipIntegration -ContinueOnFailure
```
