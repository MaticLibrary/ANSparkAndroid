# 🔧 SPECYFIKACJA BACKENDU - ANSpark

## ✅ NAPRWIONE BŁĘDY W FRONTEND

Wszystkie błędy kompilacji frontendu zostały naprawione:

### 1. **MatchApi.java** - WYCZYSZCZONY I NAPRAWIONY
```java
@GET("matches") → Call<List<Match>>
@POST("decisions") → Call<Match> sendDecision()
@POST("likes/") → Call<Map<String, Object>> like()
@GET("likes/matches") → Call<List<Match>> getLikedMatches()
```

### 2. **ChatViewModel.java** - Naprawiony typ parametru
```java
// ❌ BYŁO:
public void loadMessages(String chatId)
public void sendMessage(String chatId, String text)

// ✅ TERAZ:
public void loadMessages(Long chatId)
public void sendMessage(Long chatId, String text)
```

### 3. **MatchRepository.java** - Dodana metoda sendDecision()
```java
public void sendDecision(Profile profile, boolean liked, RepositoryCallback<Match> callback)
```

### 4. **ProfileRepository.java** - Naprawiony uploadPhoto()
```java
// ❌ BYŁO:
public void uploadPhoto(Photo photo, RepositoryCallback<Photo> callback)

// ✅ TERAZ:
public void uploadPhoto(File photoFile, RepositoryCallback<Photo> callback)
// Konwertuje File → MultipartBody.Part
```

### 5. **WebSocketService.java** - Naprawiony konflikt interfejsów
```java
// ❌ BYŁO: public interface WebSocketListener (konflikt z okhttp3)
// ✅ TERAZ: public interface SocketListener

// ✅ Dodany import: okhttp3.ByteString
// ✅ Naprawione metody onMessage() dla ByteString
```

---

## 🏗️ STRUKTURA BACKENDU - SPRING BOOT + DOCKER

### **Docker Compose (docker-compose.yml)**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: ANSpark_db
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
      POSTGRES_DB: anspark_db
    ports:
      - "5555:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - anspark-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin"]
      interval: 10s
      timeout: 5s
      retries: 5

  pgadmin:
    image: dpage/pgadmin4
    container_name: ANSpark_pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: admin123
    ports:
      - "5050:80"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - anspark-network
    volumes:
      - pgadmin_data:/var/lib/pgadmin

  anspark-backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ANSpark_backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/anspark_db
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: admin123
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      JWT_SECRET: your-secret-key-min-32-chars
      FILE_UPLOAD_PATH: /app/uploads
      CORS_ALLOWED_ORIGINS: http://localhost:8080,http://10.0.2.2:8080
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - anspark-network
    volumes:
      - ./uploads:/app/uploads
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_data:
  pgadmin_data:

networks:
  anspark-network:
    driver: bridge
```

---

## 📋 ENDPOINTY API - CO BACKEND MUSI IMPLEMENTOWAĆ

### **1. AUTHENTICATION (AuthApi.java)**
```
POST   /api/auth/register          - RegisterRequest → AuthResponse
POST   /api/auth/login             - LoginRequest → AuthResponse
POST   /api/auth/logout            - (Protected) → Void
```

**Request/Response:**
```json
// RegisterRequest
{
  "email": "user@example.com",
  "password": "password123",
  "passwordConfirm": "password123"
}

// LoginRequest
{
  "email": "user@example.com",
  "password": "password123"
}

// AuthResponse
{
  "token": "eyJhbGc...",
  "user": {
    "id": "user_123",
    "email": "user@example.com",
    "profile": { ... }
  }
}
```

---

### **2. PROFILE (ProfileApi.java)**
```
GET    /api/profile/me             - (Protected) → Profile
PUT    /api/profile/me             - (Protected) + Profile → Profile
POST   /api/profile/me/photo       - (Protected) + Multipart → Photo
```

**Request/Response:**
```json
// Profile
{
  "id": "user_123",
  "name": "John Doe",
  "age": 25,
  "bio": "Software developer",
  "city": "Warsaw",
  "tags": ["tech", "music"],
  "photos": [
    {
      "id": "photo_1",
      "url": "https://cdn.example.com/photos/photo_1.jpg",
      "primary": true
    }
  ]
}

// Photo (Response)
{
  "id": "photo_123",
  "url": "https://cdn.example.com/photos/photo_123.jpg",
  "primary": false
}
```

---

### **3. DISCOVER (DiscoverApi.java)**
```
GET    /api/discover?page=1&limit=10  - (Protected) → PaginationResponse<Profile>
```

**Response:**
```json
{
  "items": [ { Profile }, { Profile }, ... ],
  "totalCount": 150,
  "page": 1,
  "totalPages": 15,
  "hasNext": true,
  "hasPrev": false
}
```

---

### **4. DECISIONS & LIKES (MatchApi.java) - **KLUCZOWE**

#### **4a. SEND DECISION (Like/Dislike)**
```
POST   /api/decisions              - (Protected) + DecisionRequest → Match
```

**Request:**
```json
{
  "profile_id": "user_456",
  "liked": true                    // true = Like, false = Dislike
}
```

**Response:**
```json
{
  "id": "match_123",
  "profile": { Profile object },
  "liked": true,
  "matched_at": "2026-03-18T10:30:00Z"
}
```

#### **4b. QUICK LIKE (Serduszko)**
```
POST   /api/likes/                 - (Protected) + LikeRequest → Map
```

**Request:**
```json
{
  "liked_profile_id": "user_456"
}
```

**Response:**
```json
{
  "success": true,
  "profileId": "user_456",
  "timestamp": "2026-03-18T10:30:00Z"
}
```

#### **4c. GET ALL MATCHES (Results)**
```
GET    /api/matches               - (Protected) → List<Match>
```

#### **4d. GET LIKED MATCHES**
```
GET    /api/likes/matches         - (Protected) → List<Match>
```

---

### **5. MESSAGES & CHATS (ChatApi.java)**

#### **5a. GET ALL CHATS**
```
GET    /api/chats                 - (Protected) → List<Chat>
```

**Response:**
```json
[
  {
    "id": "chat_1",
    "participant": { Profile },
    "last_message": {
      "id": "msg_1",
      "text": "Cześć!",
      "sender_id": "user_456",
      "created_at": "2026-03-18T10:30:00Z",
      "outgoing": false
    },
    "last_message_at": "2026-03-18T10:30:00Z"
  }
]
```

#### **5b. GET MESSAGES FOR CHAT**
```
GET    /api/matches/{matchId}/messages    - (Protected) → List<Message>
```

**Response:**
```json
[
  {
    "id": "msg_1",
    "chat_id": "chat_1",
    "sender_id": "user_456",
    "text": "Cześć!",
    "created_at": "2026-03-18T10:30:00Z",
    "outgoing": false
  }
]
```

#### **5c. SEND MESSAGE**
```
POST   /api/matches/{matchId}/messages    - (Protected) + String (raw body) → Message
```

**Request:** Plain text
```
Cześć! Jak się masz?
```

**Response:**
```json
{
  "id": "msg_2",
  "chat_id": "chat_1",
  "sender_id": "user_123",
  "text": "Cześć! Jak się masz?",
  "created_at": "2026-03-18T10:31:00Z",
  "outgoing": true
}
```

---

### **6. WEBSOCKET (STOMP) - Real-time Messages**
```
WS     ws://localhost:8080/api/ws-chat
```

**STOMP Topics:**
```
/topic/messages/{chatId}         - Subscriber dla nowych wiadomości
/app/chat/{chatId}               - Publisher (wysyłanie wiadomości)
/app/connect                      - Subscribe na koneksję
```

**STOMP Frame:**
```
CONNECT
accept-version:1.0,1.1,1.2
heart-beat:0,0

SUBSCRIBE
id:0
destination:/topic/messages/chat_1

MESSAGE
destination:/topic/messages/chat_1
content-length:20

{"text":"Cześć!","timestamp":"..."}
```

---

## 🗄️ STRUKTURA BAZY DANYCH - PostgreSQL

### **Tabele do stworzenia:**

```sql
-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Profiles
CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    age INT CHECK (age >= 18),
    bio TEXT,
    city VARCHAR(255),
    tags VARCHAR(255),  -- JSON lub comma-separated
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Photos
CREATE TABLE photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Decisions (Like/Dislike)
CREATE TABLE decisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    target_user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    decision BOOLEAN NOT NULL,  -- true = Like, false = Dislike
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, target_user_id)
);

-- Matches (Mutual Likes)
CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_1_id UUID REFERENCES users(id) ON DELETE CASCADE,
    user_2_id UUID REFERENCES users(id) ON DELETE CASCADE,
    matched_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_1_id, user_2_id)
);

-- Chats
CREATE TABLE chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID REFERENCES matches(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Messages
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID REFERENCES chats(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_profiles_user_id ON profiles(user_id);
CREATE INDEX idx_photos_user_id ON photos(user_id);
CREATE INDEX idx_decisions_user_id ON decisions(user_id);
CREATE INDEX idx_matches_users ON matches(user_1_id, user_2_id);
CREATE INDEX idx_messages_chat_id ON messages(chat_id);
CREATE INDEX idx_messages_created ON messages(created_at DESC);
```

---

## 🚀 KROKI IMPLEMENTACJI BACKENDU

### **Krok 1: Inicjalizacja projektu**
```bash
# Maven
mvn -X archetype:generate \
  -DgroupId=com.anspark \
  -DartifactId=anspark-backend \
  -DarchetypeArtifactId=maven-archetype-quickstart

cd anspark-backend
```

### **Krok 2: pom.xml - Zależności**
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.2.3</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
        <version>3.2.3</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        <version>3.2.3</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
        <version>3.2.3</version>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.0</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- File Upload -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.11.0</version>
    </dependency>
</dependencies>
```

### **Krok 3: application.yml**
```yaml
spring:
  application:
    name: anspark-backend
  
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5555/anspark_db}
    username: ${DB_USER:admin}
    password: ${DB_PASSWORD:admin123}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  
  cors:
    allowed-origins: http://localhost:8080,http://10.0.2.2:8080
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: '*'
    max-age: 3600

server:
  port: 8080
  servlet:
    context-path: /api

jwt:
  secret: ${JWT_SECRET:your-secret-key-must-be-at-least-256-bits-long-for-security}
  expiration: 86400000  # 24h

file:
  upload-path: ${FILE_UPLOAD_PATH:/app/uploads}
```

### **Krok 4: Entities (JPA Models)**
```java
// User.java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

// Profile.java
@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
public class Profile {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String name;
    
    private Integer age;
    private String bio;
    private String city;
    
    @ElementCollection
    @CollectionTable(name = "profile_tags")
    private List<String> tags = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Photo> photos = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

// Photo.java
@Entity
@Table(name = "photos")
@Data
@NoArgsConstructor
public class Photo {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String url;
    
    @Column(name = "is_primary")
    private Boolean primary = false;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

// Decision.java
@Entity
@Table(name = "decisions")
@Data
@NoArgsConstructor
public class Decision {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private User targetUser;
    
    @Column(nullable = false)
    private Boolean decision;  // true = Like, false = Dislike
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UniqueConstraint(columnNames = {"user_id", "target_user_id"})
}

// Match.java
@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
public class Match {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "user_1_id")
    private User user1;
    
    @ManyToOne
    @JoinColumn(name = "user_2_id")
    private User user2;
    
    @CreationTimestamp
    private LocalDateTime matchedAt;
}

// Chat.java
@Entity
@Table(name = "chats")
@Data
@NoArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;
    
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}

// Message.java
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
    
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

### **Krok 5: Repositories (Spring Data JPA)**
```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUserId(UUID userId);
}

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    List<Photo> findByUserId(UUID userId);
    List<Photo> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

public interface DecisionRepository extends JpaRepository<Decision, UUID> {
    Optional<Decision> findByUserIdAndTargetUserId(UUID userId, UUID targetUserId);
    List<Decision> findByUserIdAndDecisionTrue(UUID userId);
}

public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByUser1IdOrUser2Id(UUID user1Id, UUID user2Id);
    Optional<Match> findByUser1IdAndUser2Id(UUID user1Id, UUID user2Id);
}

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    Optional<Chat> findByMatchId(UUID matchId);
}

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByChatIdOrderByCreatedAtAsc(UUID chatId);
}
```

### **Krok 6: Services (Business Logic)**
```java
// AuthService
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email już istnieje");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        
        String token = jwtProvider.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token, saved);
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Błędne hasło");
        }
        
        String token = jwtProvider.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user);
    }
}

// ProfileService
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    
    public Profile getMyProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil nie znaleziony"));
    }
    
    public Profile updateProfile(UUID userId, Profile updated) {
        Profile profile = getMyProfile(userId);
        profile.setName(updated.getName());
        profile.setAge(updated.getAge());
        profile.setBio(updated.getBio());
        profile.setCity(updated.getCity());
        profile.setTags(updated.getTags());
        return profileRepository.save(profile);
    }
}

// DiscoverService
@Service
@RequiredArgsConstructor
public class DiscoverService {
    private final ProfileRepository profileRepository;
    private final DecisionRepository decisionRepository;
    
    public List<Profile> getProfilesToDiscover(UUID userId, int page, int limit) {
        // Pobrać profi ile które użytkownik jeszcze nie ocenił
        Pageable pageable = PageRequest.of(page - 1, limit);
        
        // Logika: zwróć profile które NIE są w DecisionRepository dla tego użytkownika
        return profileRepository.findAll(pageable)
                .stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .filter(p -> decisionRepository.findByUserIdAndTargetUserId(userId, p.getUser().getId()).isEmpty())
                .collect(Collectors.toList());
    }
}

// DecisionService
@Service
@RequiredArgsConstructor
public class DecisionService {
    private final DecisionRepository decisionRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    
    public Match recordDecision(UUID userId, UUID targetUserId, boolean liked) {
        Decision decision = new Decision();
        decision.setUser(userRepository.getReferenceById(userId));
        decision.setTargetUser(userRepository.getReferenceById(targetUserId));
        decision.setDecision(liked);
        decisionRepository.save(decision);
        
        // Sprawdzić czy jest wzajemne polubienie
        if (liked) {
            Optional<Decision> reciprocal = decisionRepository
                    .findByUserIdAndTargetUserId(targetUserId, userId);
            
            if (reciprocal.isPresent() && reciprocal.get().getDecision()) {
                return createMatch(userId, targetUserId);
            }
        }
        
        return null;
    }
    
    private Match createMatch(UUID user1Id, UUID user2Id) {
        Match match = new Match();
        match.setUser1(userRepository.getReferenceById(user1Id));
        match.setUser2(userRepository.getReferenceById(user2Id));
        return matchRepository.save(match);
    }
}

// ChatService
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    
    public Chat getOrCreateChat(UUID matchId) {
        return chatRepository.findByMatchId(matchId)
                .orElseGet(() -> {
                    Chat chat = new Chat();
                    chat.setMatch(matchRepository.getReferenceById(matchId));
                    return chatRepository.save(chat);
                });
    }
    
    public List<Message> getMessages(UUID chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }
    
    public Message sendMessage(UUID chatId, UUID senderId, String text) {
        Message message = new Message();
        message.setChat(chatRepository.getReferenceById(chatId));
        message.setSender(new User() {{ setId(senderId); }});
        message.setText(text);
        return messageRepository.save(message);
    }
}
```

### **Krok 7: Controllers (REST Endpoints)**
```java
// AuthController
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout() {
        // JWT jest bezstanowy, logout to tylko oczyszczenie frontendu
        return ResponseEntity.ok().build();
    }
}

// ProfileController
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private final ProfileService profileService;
    private final FileStorageService fileStorageService;
    private final CurrentUserProvider currentUserProvider;
    
    @GetMapping("/me")
    public ResponseEntity<Profile> getProfile() {
        UUID userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(profileService.getMyProfile(userId));
    }
    
    @PutMapping("/me")
    public ResponseEntity<Profile> updateProfile(@RequestBody Profile profile) {
        UUID userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(profileService.updateProfile(userId, profile));
    }
    
    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Photo> uploadPhoto(@RequestParam("file") MultipartFile file) {
        UUID userId = currentUserProvider.getCurrentUserId();
        String filename = fileStorageService.save(file, userId);
        Photo photo = new Photo();
        photo.setUrl("/uploads/" + filename);
        return ResponseEntity.ok(photo);
    }
}

// DiscoverController
@RestController
@RequestMapping("/discover")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DiscoverController {
    private final DiscoverService discoverService;
    private final CurrentUserProvider currentUserProvider;
    
    @GetMapping
    public ResponseEntity<PaginationResponse<Profile>> discover(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        UUID userId = currentUserProvider.getCurrentUserId();
        List<Profile> profiles = discoverService.getProfilesToDiscover(userId, page, limit);
        return ResponseEntity.ok(new PaginationResponse<>(profiles, profiles.size(), page, 1, false, false));
    }
}

// DecisionController
@RestController
@RequestMapping
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DecisionController {
    private final DecisionService decisionService;
    private final CurrentUserProvider currentUserProvider;
    
    @PostMapping("/decisions")
    public ResponseEntity<Match> recordDecision(@RequestBody DecisionRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        Match match = decisionService.recordDecision(uuid userId, UUID.fromString(request.getProfile_id()), request.isLiked());
        return ResponseEntity.ok(match);
    }
    
    @PostMapping("/likes/")
    public ResponseEntity<Map<String, Object>> like(@RequestBody Map<String, Object> request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        UUID targetId = UUID.fromString((String) request.get("liked_profile_id"));
        decisionService.recordDecision(userId, targetId, true);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("profileId", targetId);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}

// ChatController
@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {
    private final ChatService chatService;
    
    @GetMapping("/{matchId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable UUID matchId) {
        Chat chat = chatService.getOrCreateChat(matchId);
        return ResponseEntity.ok(chatService.getMessages(chat.getId()));
    }
    
    @PostMapping("/{matchId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID matchId,
            @RequestBody String text,
            @AuthenticationPrincipal UserDetails userDetails) {
        Chat chat = chatService.getOrCreateChat(matchId);
        UUID senderId = UUID.fromString(userDetails.getUsername());
        Message message = chatService.sendMessage(chat.getId(), senderId, text);
        return ResponseEntity.ok(message);
    }
}
```

### **Krok 8: WebSocket Configuration (STOMP)**
```java
// WebSocketConfig
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

// StompController
@Controller
@RequiredArgsConstructor
public class StompController {
    
    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/messages/{chatId}")
    public Message handleChatMessage(
            @DestinationVariable String chatId,
            @Payload Message message,
            Principal principal) {
        message.setSender(new User() {{ setId(UUID.fromString(principal.getName())); }});
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("User connected: {}", event.getUser());
    }
}
```

### **Krok 9: Security (JWT)**
```java
// JwtProvider
@Component
@RequiredArgsConstructor
public class JwtProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    public String generateToken(UUID userId, String email) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

// JwtAuthenticationFilter
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (jwt != null && jwtProvider.validateToken(jwt)) {
                UUID userId = jwtProvider.getUserIdFromToken(jwt);
                UserDetails userDetails = new User(userId.toString(), "", new ArrayList<>());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

// SecurityConfig
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors()
                .and()
                .authorizeRequests()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/ws-chat/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### **Krok 10: Dockerfile**
```dockerfile
FROM eclipse-temurinMaven 3.8.1 with OpenJDK 21
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=0 /build/target/*.jar app.jar
RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 📝 UWAGI FINALNE

1. **Frontend czeka na Backend** - Wszystkie błędy frontendu naprawione
2. **Endpointy muszą być exactnie** takie jak opisane
3. **JWT Token** musi być w nagłówku: `Authorization: Bearer {token}`
4. **CORS** musi zezwalać na `http://10.0.2.2:8080` (Android emulator)
5. **WebSocket** standardowy STOMP protocol - frontend już to obsługuje
6. **Baza danych** - Strukturę SQL podałem wyżej

---

## 🚀 URUCHOMIENIE

```bash
# Terminal 1: Baza danych i serwisy
cd /path/to/docker-compose
docker-compose up -d

# Terminal 2: Backend
cd anspark-backend
mvn spring-boot:run

# Sprawdzenie:
curl http://localhost:8080/api/health
```

**PgAdmin:** http://localhost:5050 (admin@admin.com / admin123)
**Backend:** http://localhost:8080/api

---

Powodzenia! 🚀
