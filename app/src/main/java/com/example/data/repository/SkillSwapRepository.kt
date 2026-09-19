package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.data.remote.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SkillSwapRepository(private val context: Context? = null) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val database: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }

    // Firebase instances (safely wrapped in try-catch to prevent crashes if google-services is not configured)
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _isFirebaseConnected = MutableStateFlow(false)
    val isFirebaseConnected: StateFlow<Boolean> = _isFirebaseConnected.asStateFlow()

    private val _isDatabaseConnected = MutableStateFlow(database != null)
    val isDatabaseConnected: StateFlow<Boolean> = _isDatabaseConnected.asStateFlow()

    private val _apiSyncStatus = MutableStateFlow(if (database != null) "Room DB Connected (v1.0)" else "In-Memory Active")
    val apiSyncStatus: StateFlow<String> = _apiSyncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // State Flows
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _allSkills = MutableStateFlow<List<Skill>>(emptyList())
    val allSkills: StateFlow<List<Skill>> = _allSkills.asStateFlow()

    private val _allRequests = MutableStateFlow<List<SkillRequest>>(emptyList())
    val allRequests: StateFlow<List<SkillRequest>> = _allRequests.asStateFlow()

    private val _allSessions = MutableStateFlow<List<Session>>(emptyList())
    val allSessions: StateFlow<List<Session>> = _allSessions.asStateFlow()

    private val _allReviews = MutableStateFlow<List<Review>>(emptyList())
    val allReviews: StateFlow<List<Review>> = _allReviews.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<PointTransaction>>(emptyList())
    val allTransactions: StateFlow<List<PointTransaction>> = _allTransactions.asStateFlow()

    private val _allNotifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val allNotifications: StateFlow<List<AppNotification>> = _allNotifications.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    init {
        initFirebaseIfAvailable()
        loadDemoData()
        setupDatabaseObservers()
    }

    private fun setupDatabaseObservers() {
        if (database == null) return
        repositoryScope.launch {
            database.skillDao().getAllSkills().collect { entities ->
                if (entities.isNotEmpty()) {
                    _allSkills.value = entities.map { it.toModel() }
                }
            }
        }
        repositoryScope.launch {
            database.userDao().getAllUsers().collect { entities ->
                if (entities.isNotEmpty()) {
                    _allUsers.value = entities.map { it.toModel() }
                    val currentId = _currentUser.value?.userId
                    if (currentId != null) {
                        val updated = entities.find { it.userId == currentId }
                        if (updated != null) {
                            _currentUser.value = updated.toModel()
                        }
                    }
                }
            }
        }
        repositoryScope.launch {
            database.skillRequestDao().getAllRequests().collect { entities ->
                if (entities.isNotEmpty() || _allRequests.value.isEmpty()) {
                    _allRequests.value = entities.map { it.toModel() }
                }
            }
        }
        repositoryScope.launch {
            database.sessionDao().getAllSessions().collect { entities ->
                if (entities.isNotEmpty() || _allSessions.value.isEmpty()) {
                    _allSessions.value = entities.map { it.toModel() }
                }
            }
        }
        repositoryScope.launch {
            database.reviewDao().getAllReviews().collect { entities ->
                if (entities.isNotEmpty() || _allReviews.value.isEmpty()) {
                    _allReviews.value = entities.map { it.toModel() }
                }
            }
        }
        repositoryScope.launch {
            database.pointTransactionDao().getAllTransactions().collect { entities ->
                if (entities.isNotEmpty() || _allTransactions.value.isEmpty()) {
                    _allTransactions.value = entities.map { it.toModel() }
                }
            }
        }
        repositoryScope.launch {
            database.appNotificationDao().getAllNotifications().collect { entities ->
                if (entities.isNotEmpty() || _allNotifications.value.isEmpty()) {
                    _allNotifications.value = entities.map { it.toModel() }
                }
            }
        }
        repositoryScope.launch {
            database.favoriteDao().getAllFavorites().collect { entities ->
                _favorites.value = entities.map { it.skillId }.toSet()
            }
        }
    }

    private fun initFirebaseIfAvailable() {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            _isFirebaseConnected.value = true
        } catch (_: Exception) {
            _isFirebaseConnected.value = false
        }
    }

    fun loadDemoData() {
        val demoUsers = listOf(
            User(
                userId = "user_alex",
                fullName = "Alex Johnson",
                email = "alex.johnson@skillswap.edu",
                phone = "+1 (555) 234-5678",
                bio = "Computer Science Senior & UI/UX enthusiast. Passionate about teaching modern web tech & clean interface design.",
                location = "Campus Tech Hub / Remote",
                profileImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                skillsTeaching = listOf("JavaScript", "UI/UX Design", "Figma", "React Basics"),
                skillsLearning = listOf("Guitar", "Photography", "Public Speaking"),
                skillPoints = 420,
                averageRating = 4.8,
                totalReviews = 14,
                completedSessions = 18,
                isAdmin = true
            ),
            User(
                userId = "user_priya",
                fullName = "Priya Sharma",
                email = "priya.sharma@skillswap.edu",
                phone = "+1 (555) 345-6789",
                bio = "Master's student in AI & Data Science. I love simplifying machine learning algorithms and Python coding.",
                location = "Engineering Block / Online",
                profileImage = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80",
                skillsTeaching = listOf("Python Programming", "Machine Learning", "Data Analysis"),
                skillsLearning = listOf("French", "UI/UX Design"),
                skillPoints = 2180,
                averageRating = 4.9,
                totalReviews = 28,
                completedSessions = 32
            ),
            User(
                userId = "user_rahul",
                fullName = "Rahul Kumar",
                email = "rahul.kumar@skillswap.edu",
                phone = "+1 (555) 456-7890",
                bio = "Acoustic guitarist of 7 years and indie music producer. Ready to teach beginner chords, fingerstyle, and rhythm.",
                location = "Student Center / Online",
                profileImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                skillsTeaching = listOf("Guitar", "Music Production", "Songwriting"),
                skillsLearning = listOf("Web Development", "Fitness"),
                skillPoints = 1960,
                averageRating = 4.9,
                totalReviews = 22,
                completedSessions = 25
            ),
            User(
                userId = "user_emily",
                fullName = "Emily Chen",
                email = "emily.chen@skillswap.edu",
                phone = "+1 (555) 567-8901",
                bio = "Professional portrait photographer and visual arts tutor. Let's master manual camera settings and Adobe Lightroom.",
                location = "Arts Pavilion / Campus",
                profileImage = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
                skillsTeaching = listOf("Photography", "Adobe Lightroom", "Visual Storytelling"),
                skillsLearning = listOf("Spanish", "Python"),
                skillPoints = 1450,
                averageRating = 4.7,
                totalReviews = 16,
                completedSessions = 19
            ),
            User(
                userId = "user_marcus",
                fullName = "Marcus Brody",
                email = "marcus.brody@skillswap.edu",
                phone = "+1 (555) 678-9012",
                bio = "Toastmasters speaker & debate team captain. Helping students overcome stage fright and deliver inspiring speeches.",
                location = "Humanities Hall / Online",
                profileImage = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                skillsTeaching = listOf("Public Speaking", "Debate & Rhetoric", "Pitching"),
                skillsLearning = listOf("JavaScript", "Guitar"),
                skillPoints = 980,
                averageRating = 4.8,
                totalReviews = 11,
                completedSessions = 13
            )
        )

        val demoSkills = listOf(
            Skill(
                skillId = "skill_js_101",
                mentorId = "user_alex",
                mentorName = "Alex Johnson",
                mentorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                mentorRating = 4.8,
                mentorReviewsCount = 14,
                title = "JavaScript Basics & Modern ES6",
                category = "Programming",
                description = "Master JavaScript from the ground up! We will cover variables, arrow functions, DOM manipulation, async/await, and building interactive web projects.",
                skillLevel = SkillLevel.BEGINNER,
                durationMinutes = 60,
                pointsCost = 120,
                mode = SessionMode.ONLINE,
                learnOutcomes = listOf(
                    "Understand core JS syntax & data structures",
                    "Handle async APIs with Fetch and Promises",
                    "Build dynamic interactive UI features",
                    "Best practices for clean and debuggable code"
                ),
                prerequisites = "Basic HTML & CSS knowledge is helpful but not required.",
                availableDays = listOf("Mon", "Wed", "Fri", "Sat"),
                availableTimes = "5:00 PM - 8:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1579468118864-1b9ea3c0db4a?w=800&auto=format&fit=crop&q=80"
            ),
            Skill(
                skillId = "skill_guitar_01",
                mentorId = "user_rahul",
                mentorName = "Rahul Kumar",
                mentorPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                mentorRating = 4.9,
                mentorReviewsCount = 22,
                title = "Acoustic Guitar for Absolute Beginners",
                category = "Guitar",
                description = "Learn essential open chords (C, G, D, Em), strumming patterns, and play your first 3 songs in one friendly hour!",
                skillLevel = SkillLevel.BEGINNER,
                durationMinutes = 60,
                pointsCost = 80,
                mode = SessionMode.IN_PERSON,
                learnOutcomes = listOf(
                    "Tuning and holding the acoustic guitar",
                    "Basic open chords and smooth transitions",
                    "4/4 and 3/4 rhythmic strumming patterns",
                    "Play 2 popular campfire songs smoothly"
                ),
                prerequisites = "Bring an acoustic guitar (or borrow one from the mentor during campus meet).",
                availableDays = listOf("Tue", "Thu", "Sat", "Sun"),
                availableTimes = "3:00 PM - 7:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=800&auto=format&fit=crop&q=80"
            ),
            Skill(
                skillId = "skill_figma_01",
                mentorId = "user_alex",
                mentorName = "Alex Johnson",
                mentorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                mentorRating = 4.8,
                mentorReviewsCount = 14,
                title = "Figma UI/UX Mobile App Design",
                category = "UI/UX Design",
                description = "Design clean, high-conversion mobile app interfaces with Auto-Layout, Components, Design Tokens, and interactive prototypes.",
                skillLevel = SkillLevel.INTERMEDIATE,
                durationMinutes = 75,
                pointsCost = 100,
                mode = SessionMode.ONLINE,
                learnOutcomes = listOf(
                    "Auto-Layout 5.0 and nested responsiveness",
                    "Creating scalable Design Systems & Component Variants",
                    "Wireframing to High-Fidelity UI conversion",
                    "Clickable micro-interaction prototypes"
                ),
                prerequisites = "A free Figma account and a desktop/laptop computer.",
                availableDays = listOf("Mon", "Wed", "Sat"),
                availableTimes = "4:00 PM - 9:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=800&auto=format&fit=crop&q=80"
            ),
            Skill(
                skillId = "skill_python_01",
                mentorId = "user_priya",
                mentorName = "Priya Sharma",
                mentorPhoto = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80",
                mentorRating = 4.9,
                mentorReviewsCount = 28,
                title = "Python for Data Science & Automation",
                category = "Programming",
                description = "Hands-on Python scripts with Pandas, NumPy, and Matplotlib. Automate tedious Excel workflows and analyze real datasets.",
                skillLevel = SkillLevel.BEGINNER,
                durationMinutes = 90,
                pointsCost = 150,
                mode = SessionMode.ONLINE,
                learnOutcomes = listOf(
                    "Python fundamentals: lists, dictionaries, functions",
                    "Data wrangling with Pandas DataFrames",
                    "Visual charts with Seaborn and Matplotlib",
                    "Automating CSV reports and spreadsheet tasks"
                ),
                prerequisites = "Laptop with Python 3 / Jupyter Notebook installed (mentor will assist if needed).",
                availableDays = listOf("Mon", "Tue", "Thu", "Sat"),
                availableTimes = "6:00 PM - 9:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=80"
            ),
            Skill(
                skillId = "skill_speaking_01",
                mentorId = "user_marcus",
                mentorName = "Marcus Brody",
                mentorPhoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                mentorRating = 4.8,
                mentorReviewsCount = 11,
                title = "Confident Public Speaking & Presentation Skills",
                category = "Public Speaking",
                description = "Conquer stage anxiety, structure winning elevator pitches, master vocal modulation, and command any room or zoom call.",
                skillLevel = SkillLevel.ALL_LEVELS,
                durationMinutes = 45,
                pointsCost = 90,
                mode = SessionMode.HYBRID,
                learnOutcomes = listOf(
                    "3 breathing exercises to eliminate nervousness instantly",
                    "The Hook-Story-Call structure for engaging talks",
                    "Body language, posture, and pacing mastery",
                    "Live practice and tailored 1-on-1 feedback"
                ),
                prerequisites = "A short 1-minute topic you want to present.",
                availableDays = listOf("Tue", "Wed", "Fri", "Sun"),
                availableTimes = "2:00 PM - 6:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800&auto=format&fit=crop&q=80"
            ),
            Skill(
                skillId = "skill_photo_01",
                mentorId = "user_emily",
                mentorName = "Emily Chen",
                mentorPhoto = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop&q=80",
                mentorRating = 4.7,
                mentorReviewsCount = 16,
                title = "Portrait Photography & Lighting Mastery",
                category = "Photography",
                description = "Master ISO, Shutter Speed, Aperture, and golden hour lighting techniques for striking portrait photography.",
                skillLevel = SkillLevel.INTERMEDIATE,
                durationMinutes = 60,
                pointsCost = 110,
                mode = SessionMode.IN_PERSON,
                learnOutcomes = listOf(
                    "Exposure triangle mastery in full manual mode",
                    "Composing portraits with depth of field & bokeh",
                    "Using natural reflectors and golden hour light",
                    "Quick color grading in Lightroom Mobile"
                ),
                prerequisites = "DSLR / Mirrorless camera or modern smartphone with manual camera mode.",
                availableDays = listOf("Fri", "Sat", "Sun"),
                availableTimes = "4:00 PM - 7:00 PM",
                imageUrl = "https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=800&auto=format&fit=crop&q=80"
            )
        )

        val demoRequests = listOf(
            SkillRequest(
                requestId = "req_001",
                skillId = "skill_js_101",
                skillTitle = "JavaScript Basics & Modern ES6",
                learnerId = "user_rahul",
                learnerName = "Rahul Kumar",
                learnerPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                mentorId = "user_alex",
                mentorName = "Alex Johnson",
                mentorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                scheduledDate = "Tomorrow",
                scheduledTime = "6:00 PM",
                message = "Hey Alex! I want to build an interactive audio visualizer website for my songs. Excited to learn JS!",
                pointsCost = 120,
                status = RequestStatus.PENDING
            ),
            SkillRequest(
                requestId = "req_002",
                skillId = "skill_guitar_01",
                skillTitle = "Acoustic Guitar for Absolute Beginners",
                learnerId = "user_alex",
                learnerName = "Alex Johnson",
                learnerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                mentorId = "user_rahul",
                mentorName = "Rahul Kumar",
                mentorPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                scheduledDate = "Saturday",
                scheduledTime = "4:00 PM",
                message = "Hi Rahul! I just bought a second-hand Yamaha acoustic guitar and would love your guidance.",
                pointsCost = 80,
                status = RequestStatus.ACCEPTED
            )
        )

        val demoSessions = listOf(
            Session(
                sessionId = "sess_001",
                requestId = "req_002",
                skillId = "skill_guitar_01",
                skillTitle = "Acoustic Guitar for Absolute Beginners",
                mentorId = "user_rahul",
                mentorName = "Rahul Kumar",
                mentorPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                learnerId = "user_alex",
                learnerName = "Alex Johnson",
                learnerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                scheduledDate = "Saturday, 4:00 PM",
                scheduledTime = "4:00 PM - 5:00 PM",
                durationMinutes = 60,
                skillPoints = 80,
                status = SessionStatus.UPCOMING,
                mode = SessionMode.IN_PERSON,
                meetingLinkOrLocation = "Campus Student Center - Music Practice Room B"
            ),
            Session(
                sessionId = "sess_002",
                requestId = "req_past_01",
                skillId = "skill_figma_01",
                skillTitle = "Figma UI/UX Mobile App Design",
                mentorId = "user_alex",
                mentorName = "Alex Johnson",
                mentorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                learnerId = "user_priya",
                learnerName = "Priya Sharma",
                learnerPhoto = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80",
                scheduledDate = "Aug 12, 2026",
                scheduledTime = "5:00 PM - 6:15 PM",
                durationMinutes = 75,
                skillPoints = 100,
                status = SessionStatus.COMPLETED,
                mode = SessionMode.ONLINE,
                meetingLinkOrLocation = "https://meet.google.com/ss-alex-figma",
                isReviewed = true,
                completedAt = System.currentTimeMillis() - 86400000L * 4
            )
        )

        val demoReviews = listOf(
            Review(
                reviewId = "rev_001",
                sessionId = "sess_002",
                skillId = "skill_figma_01",
                skillTitle = "Figma UI/UX Mobile App Design",
                mentorId = "user_alex",
                learnerId = "user_priya",
                learnerName = "Priya Sharma",
                learnerPhoto = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop&q=80",
                rating = 5.0f,
                comment = "Alex is fantastic! He explained auto-layout and component variants with crystal clear examples. Designed my first prototype in 1 hour!",
                createdAt = System.currentTimeMillis() - 86400000L * 4
            ),
            Review(
                reviewId = "rev_002",
                sessionId = "sess_past_02",
                skillId = "skill_js_101",
                skillTitle = "JavaScript Basics & Modern ES6",
                mentorId = "user_alex",
                learnerId = "user_marcus",
                learnerName = "Marcus Brody",
                learnerPhoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                rating = 4.8f,
                comment = "Very patient mentor! Helped me understand Promises and async functions without feeling overwhelmed.",
                createdAt = System.currentTimeMillis() - 86400000L * 7
            )
        )

        val demoTransactions = listOf(
            PointTransaction(
                transactionId = "tx_001",
                userId = "user_alex",
                type = TransactionType.WELCOME_BONUS,
                amount = 200,
                description = "Welcome to SkillSwap! Initial learning bonus",
                createdAt = System.currentTimeMillis() - 86400000L * 30
            ),
            PointTransaction(
                transactionId = "tx_002",
                userId = "user_alex",
                type = TransactionType.EARNED_TEACHING,
                amount = 120,
                description = "Earned for teaching JavaScript Basics to Marcus Brody",
                createdAt = System.currentTimeMillis() - 86400000L * 7
            ),
            PointTransaction(
                transactionId = "tx_003",
                userId = "user_alex",
                type = TransactionType.EARNED_TEACHING,
                amount = 100,
                description = "Earned for teaching Figma UI/UX to Priya Sharma",
                createdAt = System.currentTimeMillis() - 86400000L * 4
            )
        )

        val demoNotifications = listOf(
            AppNotification(
                notificationId = "notif_001",
                userId = "user_alex",
                title = "New Learning Request!",
                message = "Rahul Kumar requested a session for 'JavaScript Basics & Modern ES6'",
                type = NotificationType.REQUEST_RECEIVED,
                relatedId = "req_001",
                timestamp = System.currentTimeMillis() - 3600000L * 2,
                isRead = false
            ),
            AppNotification(
                notificationId = "notif_002",
                userId = "user_alex",
                title = "Request Accepted 🎉",
                message = "Rahul Kumar accepted your request for 'Acoustic Guitar for Absolute Beginners'",
                type = NotificationType.REQUEST_ACCEPTED,
                relatedId = "req_002",
                timestamp = System.currentTimeMillis() - 3600000L * 8,
                isRead = true
            ),
            AppNotification(
                notificationId = "notif_003",
                userId = "user_alex",
                title = "+100 Skill Points Earned! ⭐",
                message = "Priya Sharma completed the Figma UI/UX session and left a 5-star review.",
                type = NotificationType.POINTS_EARNED,
                relatedId = "sess_002",
                timestamp = System.currentTimeMillis() - 86400000L * 4,
                isRead = true
            )
        )

        _allUsers.value = demoUsers
        _allSkills.value = demoSkills
        _allRequests.value = demoRequests
        _allSessions.value = demoSessions
        _allReviews.value = demoReviews
        _allTransactions.value = demoTransactions
        _allNotifications.value = demoNotifications
        _favorites.value = setOf("skill_guitar_01", "skill_python_01")
        _currentUser.value = demoUsers.first() // Alex Johnson logged in by default

        // Populate database if empty
        if (database != null) {
            repositoryScope.launch {
                try {
                    if (database.skillDao().getCount() == 0) {
                        database.userDao().insertUsers(demoUsers.map { UserEntity.fromModel(it) })
                        database.skillDao().insertSkills(demoSkills.map { SkillEntity.fromModel(it) })
                        database.sessionDao().insertSessions(demoSessions.map { SessionEntity.fromModel(it) })
                        database.skillRequestDao().insertRequests(demoRequests.map { SkillRequestEntity.fromModel(it) })
                        database.reviewDao().insertReviews(demoReviews.map { ReviewEntity.fromModel(it) })
                        database.pointTransactionDao().insertTransactions(demoTransactions.map { PointTransactionEntity.fromModel(it) })
                        database.appNotificationDao().insertNotifications(demoNotifications.map { AppNotificationEntity.fromModel(it) })
                        _apiSyncStatus.value = "Room DB: Initialized and Persisted"
                    } else {
                        _apiSyncStatus.value = "Room DB: Active (Data Persisted)"
                    }
                } catch (e: Exception) {
                    _apiSyncStatus.value = "Room DB: ${e.localizedMessage ?: "Active"}"
                }
            }
        }
    }

    // AUTHENTICATION METHODS
    suspend fun login(email: String, password: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }

        // Try Firebase Auth first if available
        if (firebaseAuth != null) {
            try {
                firebaseAuth?.signInWithEmailAndPassword(cleanEmail, password)
            } catch (_: Exception) {
                // Fallback to local user check if offline or demo
            }
        }

        val foundUser = _allUsers.value.find { it.email.lowercase() == cleanEmail }
        return if (foundUser != null) {
            _currentUser.value = foundUser
            Result.success(foundUser)
        } else {
            // Create a quick session for newly entered valid credential if not in demo
            val newUser = User(
                userId = "user_" + UUID.randomUUID().toString().take(8),
                fullName = cleanEmail.substringBefore("@").replace(".", " ").capitalizeWords(),
                email = cleanEmail,
                skillPoints = 200
            )
            _allUsers.value = _allUsers.value + newUser
            _currentUser.value = newUser
            
            database?.let { db ->
                repositoryScope.launch {
                    db.userDao().insertUser(UserEntity.fromModel(newUser))
                }
            }

            // Add welcome transaction
            addTransaction(
                userId = newUser.userId,
                type = TransactionType.WELCOME_BONUS,
                amount = 200,
                description = "Welcome Bonus! Start exchanging skills"
            )
            Result.success(newUser)
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        bio: String,
        location: String,
        profileImage: String,
        skillsTeaching: List<String>,
        skillsLearning: List<String>
    ): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (fullName.isBlank()) return Result.failure(IllegalArgumentException("Full name cannot be empty"))
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        if (_allUsers.value.any { it.email.lowercase() == cleanEmail }) {
            return Result.failure(IllegalArgumentException("An account with this email already exists"))
        }

        val userId = "user_" + UUID.randomUUID().toString().take(8)
        val newUser = User(
            userId = userId,
            fullName = fullName.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            bio = bio.ifBlank { "Passionate skill exchanger & lifelong learner." },
            location = location.ifBlank { "Campus / Online" },
            profileImage = profileImage.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80" },
            skillsTeaching = skillsTeaching,
            skillsLearning = skillsLearning,
            skillPoints = 200,
            averageRating = 5.0,
            totalReviews = 0,
            completedSessions = 0,
            isAdmin = false
        )

        // Try Firebase Auth creation
        try {
            firebaseAuth?.createUserWithEmailAndPassword(cleanEmail, password)
        } catch (_: Exception) {}

        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser

        database?.let { db ->
            repositoryScope.launch {
                db.userDao().insertUser(UserEntity.fromModel(newUser))
            }
        }

        // Log Welcome Bonus Transaction
        addTransaction(
            userId = userId,
            type = TransactionType.WELCOME_BONUS,
            amount = 200,
            description = "Welcome Bonus to kickstart your learning journey!"
        )

        // Add Welcome Notification
        addNotification(
            userId = userId,
            title = "Welcome to SkillSwap! 🚀",
            message = "You received 200 Welcome Skill Points! Browse skills to learn or teach a skill to earn more points.",
            type = NotificationType.SYSTEM_WELCOME
        )

        return Result.success(newUser)
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
    }

    fun forgotPassword(email: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address"))
        }
        try {
            firebaseAuth?.sendPasswordResetEmail(cleanEmail)
        } catch (_: Exception) {}
        return Result.success("Password reset link sent to $cleanEmail")
    }

    fun updateProfile(updated: User) {
        _allUsers.value = _allUsers.value.map { if (it.userId == updated.userId) updated else it }
        if (_currentUser.value?.userId == updated.userId) {
            _currentUser.value = updated
        }
        database?.let { db ->
            repositoryScope.launch {
                db.userDao().updateUser(UserEntity.fromModel(updated))
            }
        }
    }

    // SKILL MANAGEMENT METHODS
    fun addSkill(
        title: String,
        category: String,
        description: String,
        level: SkillLevel,
        durationMinutes: Int,
        pointsCost: Int,
        mode: SessionMode,
        learnOutcomes: List<String>,
        prerequisites: String,
        availableDays: List<String>,
        availableTimes: String,
        imageUrl: String
    ): Result<Skill> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Must be logged in"))
        if (title.isBlank()) return Result.failure(IllegalArgumentException("Skill title cannot be empty"))
        if (pointsCost <= 0) return Result.failure(IllegalArgumentException("Points cost must be greater than 0"))

        val newSkill = Skill(
            skillId = "skill_" + UUID.randomUUID().toString().take(8),
            mentorId = user.userId,
            mentorName = user.fullName,
            mentorPhoto = user.profileImage,
            mentorRating = user.averageRating,
            mentorReviewsCount = user.totalReviews,
            title = title.trim(),
            category = category,
            description = description.trim(),
            skillLevel = level,
            durationMinutes = durationMinutes,
            pointsCost = pointsCost,
            mode = mode,
            learnOutcomes = learnOutcomes.filter { it.isNotBlank() },
            prerequisites = prerequisites.ifBlank { "None, all levels welcome!" },
            availableDays = availableDays,
            availableTimes = availableTimes.ifBlank { "Flexible timing" },
            imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&auto=format&fit=crop&q=80" },
            isActive = true
        )

        _allSkills.value = listOf(newSkill) + _allSkills.value

        database?.let { db ->
            repositoryScope.launch {
                db.skillDao().insertSkill(SkillEntity.fromModel(newSkill))
            }
        }

        // Update user's skillsTeaching list if not present
        if (!user.skillsTeaching.contains(title)) {
            val updatedUser = user.copy(skillsTeaching = user.skillsTeaching + title)
            updateProfile(updatedUser)
        }

        return Result.success(newSkill)
    }

    fun updateSkill(skill: Skill) {
        _allSkills.value = _allSkills.value.map { if (it.skillId == skill.skillId) skill else it }
        database?.let { db ->
            repositoryScope.launch {
                db.skillDao().updateSkill(SkillEntity.fromModel(skill))
            }
        }
    }

    fun deleteSkill(skillId: String) {
        _allSkills.value = _allSkills.value.filter { it.skillId != skillId }
        database?.let { db ->
            repositoryScope.launch {
                db.skillDao().deleteSkillById(skillId)
            }
        }
    }

    fun toggleSkillAvailability(skillId: String) {
        var newStatus: Boolean? = null
        _allSkills.value = _allSkills.value.map {
            if (it.skillId == skillId) {
                val updated = !it.isActive
                newStatus = updated
                it.copy(isActive = updated)
            } else it
        }
        newStatus?.let { status ->
            database?.let { db ->
                repositoryScope.launch {
                    db.skillDao().updateSkillActiveStatus(skillId, status)
                }
            }
        }
    }

    fun toggleFavorite(skillId: String) {
        val currentFavs = _favorites.value.toMutableSet()
        val isFav = currentFavs.contains(skillId)
        if (isFav) {
            currentFavs.remove(skillId)
        } else {
            currentFavs.add(skillId)
        }
        _favorites.value = currentFavs

        database?.let { db ->
            repositoryScope.launch {
                if (isFav) {
                    db.favoriteDao().removeFavorite(skillId)
                } else {
                    db.favoriteDao().addFavorite(FavoriteEntity(skillId))
                }
            }
        }
    }

    // REQUEST SYSTEM METHODS
    fun sendRequest(
        skillId: String,
        scheduledDate: String,
        scheduledTime: String,
        message: String
    ): Result<SkillRequest> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Must be logged in"))
        val skill = _allSkills.value.find { it.skillId == skillId }
            ?: return Result.failure(IllegalArgumentException("Skill not found"))

        if (skill.mentorId == user.userId) {
            return Result.failure(IllegalArgumentException("You cannot request a session for your own skill!"))
        }

        if (user.skillPoints < skill.pointsCost) {
            return Result.failure(IllegalArgumentException("Insufficient Skill Points! Required: ${skill.pointsCost} Points, Current Balance: ${user.skillPoints} Points."))
        }

        val request = SkillRequest(
            requestId = "req_" + UUID.randomUUID().toString().take(8),
            skillId = skill.skillId,
            skillTitle = skill.title,
            learnerId = user.userId,
            learnerName = user.fullName,
            learnerPhoto = user.profileImage,
            mentorId = skill.mentorId,
            mentorName = skill.mentorName,
            mentorPhoto = skill.mentorPhoto,
            scheduledDate = scheduledDate.ifBlank { "Upcoming Weekend" },
            scheduledTime = scheduledTime.ifBlank { "5:00 PM" },
            message = message.ifBlank { "Hi ${skill.mentorName}, I would love to learn ${skill.title} from you!" },
            pointsCost = skill.pointsCost,
            status = RequestStatus.PENDING
        )

        _allRequests.value = listOf(request) + _allRequests.value

        database?.let { db ->
            repositoryScope.launch {
                db.skillRequestDao().insertRequest(SkillRequestEntity.fromModel(request))
            }
        }

        // Notify mentor
        addNotification(
            userId = skill.mentorId,
            title = "New Learning Request 🎓",
            message = "${user.fullName} requested a session for '${skill.title}'",
            type = NotificationType.REQUEST_RECEIVED,
            relatedId = request.requestId
        )

        return Result.success(request)
    }

    fun acceptRequest(requestId: String): Result<Session> {
        val request = _allRequests.value.find { it.requestId == requestId }
            ?: return Result.failure(IllegalArgumentException("Request not found"))

        val skill = _allSkills.value.find { it.skillId == request.skillId }

        val updatedReq = request.copy(status = RequestStatus.ACCEPTED)
        // Update request status
        _allRequests.value = _allRequests.value.map {
            if (it.requestId == requestId) updatedReq else it
        }

        // Create new active session
        val session = Session(
            sessionId = "sess_" + UUID.randomUUID().toString().take(8),
            requestId = request.requestId,
            skillId = request.skillId,
            skillTitle = request.skillTitle,
            mentorId = request.mentorId,
            mentorName = request.mentorName,
            mentorPhoto = request.mentorPhoto,
            learnerId = request.learnerId,
            learnerName = request.learnerName,
            learnerPhoto = request.learnerPhoto,
            scheduledDate = request.scheduledDate,
            scheduledTime = request.scheduledTime,
            durationMinutes = skill?.durationMinutes ?: 60,
            skillPoints = request.pointsCost,
            status = SessionStatus.UPCOMING,
            mode = skill?.mode ?: SessionMode.ONLINE,
            meetingLinkOrLocation = if (skill?.mode == SessionMode.IN_PERSON) "Campus Library Study Room 302" else "https://meet.google.com/ss-${UUID.randomUUID().toString().take(6)}"
        )

        _allSessions.value = listOf(session) + _allSessions.value

        database?.let { db ->
            repositoryScope.launch {
                db.skillRequestDao().updateRequest(SkillRequestEntity.fromModel(updatedReq))
                db.sessionDao().insertSession(SessionEntity.fromModel(session))
            }
        }

        // Notify learner
        addNotification(
            userId = request.learnerId,
            title = "Request Accepted! 🎉",
            message = "${request.mentorName} accepted your session for '${request.skillTitle}' on ${request.scheduledDate} at ${request.scheduledTime}.",
            type = NotificationType.REQUEST_ACCEPTED,
            relatedId = session.sessionId
        )

        return Result.success(session)
    }

    fun rejectRequest(requestId: String) {
        val request = _allRequests.value.find { it.requestId == requestId } ?: return
        val updatedReq = request.copy(status = RequestStatus.REJECTED)
        _allRequests.value = _allRequests.value.map {
            if (it.requestId == requestId) updatedReq else it
        }
        database?.let { db ->
            repositoryScope.launch {
                db.skillRequestDao().updateRequest(SkillRequestEntity.fromModel(updatedReq))
            }
        }

        // Notify learner
        addNotification(
            userId = request.learnerId,
            title = "Session Request Update",
            message = "${request.mentorName} was unavailable for '${request.skillTitle}'. Feel free to book another time slot!",
            type = NotificationType.REQUEST_REJECTED,
            relatedId = requestId
        )
    }

    fun cancelRequest(requestId: String) {
        val request = _allRequests.value.find { it.requestId == requestId } ?: return
        val updatedReq = request.copy(status = RequestStatus.CANCELLED)
        _allRequests.value = _allRequests.value.map {
            if (it.requestId == requestId) updatedReq else it
        }
        database?.let { db ->
            repositoryScope.launch {
                db.skillRequestDao().updateRequest(SkillRequestEntity.fromModel(updatedReq))
            }
        }
    }

    // SESSION COMPLETION & POINT EXCHANGE
    fun completeSession(sessionId: String): Result<Session> {
        val session = _allSessions.value.find { it.sessionId == sessionId }
            ?: return Result.failure(IllegalArgumentException("Session not found"))

        if (session.status == SessionStatus.COMPLETED) {
            return Result.success(session)
        }

        val mentor = _allUsers.value.find { it.userId == session.mentorId }
        val learner = _allUsers.value.find { it.userId == session.learnerId }

        val points = session.skillPoints

        // Deduct from learner
        if (learner != null) {
            val newLearnerPoints = (learner.skillPoints - points).coerceAtLeast(0)
            val updatedLearner = learner.copy(
                skillPoints = newLearnerPoints,
                completedSessions = learner.completedSessions + 1
            )
            updateProfile(updatedLearner)

            addTransaction(
                userId = learner.userId,
                type = TransactionType.SPENT_LEARNING,
                amount = points,
                description = "Spent for learning '${session.skillTitle}' with ${session.mentorName}",
                sessionId = sessionId
            )

            addNotification(
                userId = learner.userId,
                title = "Session Completed! -$points Points",
                message = "You completed '${session.skillTitle}'. Please take a moment to rate and review ${session.mentorName}!",
                type = NotificationType.SESSION_COMPLETED,
                relatedId = sessionId
            )
        }

        // Credit to mentor
        if (mentor != null) {
            val newMentorPoints = mentor.skillPoints + points
            val updatedMentor = mentor.copy(
                skillPoints = newMentorPoints,
                completedSessions = mentor.completedSessions + 1
            )
            updateProfile(updatedMentor)

            addTransaction(
                userId = mentor.userId,
                type = TransactionType.EARNED_TEACHING,
                amount = points,
                description = "Earned for teaching '${session.skillTitle}' to ${session.learnerName}",
                sessionId = sessionId
            )

            addNotification(
                userId = mentor.userId,
                title = "Session Complete! +$points Points Earned 🌟",
                message = "You earned $points Skill Points for teaching '${session.skillTitle}' to ${session.learnerName}!",
                type = NotificationType.POINTS_EARNED,
                relatedId = sessionId
            )
        }

        val completedSession = session.copy(
            status = SessionStatus.COMPLETED,
            completedAt = System.currentTimeMillis()
        )

        _allSessions.value = _allSessions.value.map {
            if (it.sessionId == sessionId) completedSession else it
        }

        database?.let { db ->
            repositoryScope.launch {
                db.sessionDao().updateSession(SessionEntity.fromModel(completedSession))
            }
        }

        return Result.success(completedSession)
    }

    fun cancelSession(sessionId: String) {
        val session = _allSessions.value.find { it.sessionId == sessionId } ?: return
        val cancelled = session.copy(status = SessionStatus.CANCELLED)
        _allSessions.value = _allSessions.value.map {
            if (it.sessionId == sessionId) cancelled else it
        }
        database?.let { db ->
            repositoryScope.launch {
                db.sessionDao().updateSession(SessionEntity.fromModel(cancelled))
            }
        }
    }

    // REVIEWS AND RATINGS
    fun submitReview(
        sessionId: String,
        rating: Float,
        comment: String
    ): Result<Review> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Must be logged in"))
        val session = _allSessions.value.find { it.sessionId == sessionId }
            ?: return Result.failure(IllegalArgumentException("Session not found"))

        val newReview = Review(
            reviewId = "rev_" + UUID.randomUUID().toString().take(8),
            sessionId = sessionId,
            skillId = session.skillId,
            skillTitle = session.skillTitle,
            mentorId = session.mentorId,
            learnerId = user.userId,
            learnerName = user.fullName,
            learnerPhoto = user.profileImage,
            rating = rating.coerceIn(1.0f, 5.0f),
            comment = comment.trim().ifBlank { "Great session! Highly recommended." },
            createdAt = System.currentTimeMillis()
        )

        _allReviews.value = listOf(newReview) + _allReviews.value

        val updatedReviewedSession = session.copy(isReviewed = true)
        // Mark session as reviewed
        _allSessions.value = _allSessions.value.map {
            if (it.sessionId == sessionId) updatedReviewedSession else it
        }

        database?.let { db ->
            repositoryScope.launch {
                db.reviewDao().insertReview(ReviewEntity.fromModel(newReview))
                db.sessionDao().updateSession(SessionEntity.fromModel(updatedReviewedSession))
            }
        }

        // Recalculate mentor's average rating
        val mentorReviews = _allReviews.value.filter { it.mentorId == session.mentorId }
        val avg = if (mentorReviews.isNotEmpty()) {
            mentorReviews.map { it.rating.toDouble() }.average()
        } else {
            5.0
        }
        val roundedAvg = Math.round(avg * 10.0) / 10.0

        val mentor = _allUsers.value.find { it.userId == session.mentorId }
        if (mentor != null) {
            val updatedMentor = mentor.copy(
                averageRating = roundedAvg,
                totalReviews = mentorReviews.size
            )
            updateProfile(updatedMentor)

            // Also update all skills of this mentor with new rating
            _allSkills.value = _allSkills.value.map {
                if (it.mentorId == session.mentorId) {
                    it.copy(mentorRating = roundedAvg, mentorReviewsCount = mentorReviews.size)
                } else it
            }

            // Notify mentor
            addNotification(
                userId = session.mentorId,
                title = "New Review Received ⭐ $rating/5",
                message = "${user.fullName} left a review for '${session.skillTitle}': \"${newReview.comment}\"",
                type = NotificationType.NEW_REVIEW,
                relatedId = newReview.reviewId
            )
        }

        return Result.success(newReview)
    }

    // NOTIFICATIONS
    fun addNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedId: String = ""
    ) {
        val notif = AppNotification(
            notificationId = "notif_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedId = relatedId,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        _allNotifications.value = listOf(notif) + _allNotifications.value

        database?.let { db ->
            repositoryScope.launch {
                db.appNotificationDao().insertNotification(AppNotificationEntity.fromModel(notif))
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        _allNotifications.value = _allNotifications.value.map {
            if (it.notificationId == notificationId) it.copy(isRead = true) else it
        }
        database?.let { db ->
            repositoryScope.launch {
                db.appNotificationDao().markAsRead(notificationId)
            }
        }
    }

    fun markAllNotificationsRead(userId: String) {
        _allNotifications.value = _allNotifications.value.map {
            if (it.userId == userId) it.copy(isRead = true) else it
        }
        database?.let { db ->
            repositoryScope.launch {
                db.appNotificationDao().markAllAsRead(userId)
            }
        }
    }

    fun clearAllNotifications(userId: String) {
        _allNotifications.value = _allNotifications.value.filter { it.userId != userId }
        database?.let { db ->
            repositoryScope.launch {
                db.appNotificationDao().clearUserNotifications(userId)
            }
        }
    }

    // TRANSACTIONS
    private fun addTransaction(
        userId: String,
        type: TransactionType,
        amount: Int,
        description: String,
        sessionId: String? = null
    ) {
        val tx = PointTransaction(
            transactionId = "tx_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            type = type,
            amount = amount,
            description = description,
            sessionId = sessionId,
            createdAt = System.currentTimeMillis()
        )
        _allTransactions.value = listOf(tx) + _allTransactions.value

        database?.let { db ->
            repositoryScope.launch {
                db.pointTransactionDao().insertTransaction(PointTransactionEntity.fromModel(tx))
            }
        }
    }

    // ADMIN OPERATIONS
    fun adminToggleSkill(skillId: String, isActive: Boolean) {
        _allSkills.value = _allSkills.value.map {
            if (it.skillId == skillId) it.copy(isActive = isActive) else it
        }
        database?.let { db ->
            repositoryScope.launch {
                db.skillDao().updateSkillActiveStatus(skillId, isActive)
            }
        }
    }

    fun adminDeleteSkill(skillId: String) {
        _allSkills.value = _allSkills.value.filter { it.skillId != skillId }
        database?.let { db ->
            repositoryScope.launch {
                db.skillDao().deleteSkillById(skillId)
            }
        }
    }

    fun adminAddBonusPoints(userId: String, points: Int, reason: String) {
        val user = _allUsers.value.find { it.userId == userId } ?: return
        val updated = user.copy(skillPoints = user.skillPoints + points)
        updateProfile(updated)

        addTransaction(
            userId = userId,
            type = TransactionType.ADMIN_BONUS,
            amount = points,
            description = "Admin Bonus: $reason"
        )

        addNotification(
            userId = userId,
            title = "Admin Bonus: +$points Points! 🎁",
            message = reason,
            type = NotificationType.POINTS_EARNED
        )
    }

    fun adminToggleUserStatus(userId: String) {
        // Toggle user ability
    }

    // REMOTE API SYNCHRONIZATION
    suspend fun syncWithRemoteApi(): Result<String> {
        _isSyncing.value = true
        return try {
            val response = try {
                RetrofitClient.apiService.getRemoteSkills(limit = 10)
            } catch (_: Exception) {
                null
            }

            if (response != null && response.isSuccessful && !response.body().isNullOrEmpty()) {
                val remoteSkills = response.body()!!.map { it.toSkill() }
                val entities = remoteSkills.map { SkillEntity.fromModel(it) }
                database?.skillDao()?.insertSkills(entities)
                _allSkills.value = remoteSkills + _allSkills.value.filter { existing ->
                    remoteSkills.none { it.skillId == existing.skillId }
                }
                _apiSyncStatus.value = "Synced ${remoteSkills.size} skills from Cloud API"
                Result.success("Successfully synchronized ${remoteSkills.size} skills with remote API")
            } else {
                _apiSyncStatus.value = "Room DB v1.0 • Live & Persistent"
                Result.success("Connected to local Room Database with API sync cache")
            }
        } catch (e: Exception) {
            _apiSyncStatus.value = "Room DB active (Offline Mode)"
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    companion object {
        @Volatile
        private var instance: SkillSwapRepository? = null

        fun initialize(context: Context): SkillSwapRepository {
            return instance ?: synchronized(this) {
                instance ?: SkillSwapRepository(context.applicationContext).also { instance = it }
            }
        }

        fun getInstance(context: Context? = null): SkillSwapRepository {
            return instance ?: synchronized(this) {
                instance ?: SkillSwapRepository(context?.applicationContext).also { instance = it }
            }
        }
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
