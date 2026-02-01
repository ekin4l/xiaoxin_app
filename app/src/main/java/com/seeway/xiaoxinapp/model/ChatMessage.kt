package com.seeway.xiaoxinapp.model

/**
 * Chat message data model for AI assistant
 */
data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val role: MessageRole = MessageRole.USER,
    val timestamp: Long = System.currentTimeMillis(),
    val isTyping: Boolean = false,
    val intent: MessageIntent? = null
) {
    enum class MessageRole {
        USER,       // User message
        ASSISTANT,  // AI response
        SYSTEM      // System message
    }

    enum class MessageIntent {
        SEARCH_POI,        // Search for a place
        NAVIGATE,          // Start navigation
        WEATHER,           // Ask about weather
        MUSIC,             // Play music
        SETTINGS,          // Change settings
        GENERAL            // General chat
    }

    /**
     * Check if this is a user message
     */
    fun isUserMessage(): Boolean = role == MessageRole.USER

    /**
     * Check if this is an assistant message
     */
    fun isAssistantMessage(): Boolean = role == MessageRole.ASSISTANT

    /**
     * Get formatted time
     */
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "刚刚"
            diff < 3600000 -> "${diff / 60000}分钟前"
            else -> "${diff / 3600000}小时前"
        }
    }

    companion object {
        /**
         * Create a user message
         */
        fun createUserMessage(content: String, intent: MessageIntent? = null): ChatMessage {
            return ChatMessage(
                id = generateId(),
                content = content,
                role = MessageRole.USER,
                intent = intent
            )
        }

        /**
         * Create an assistant message
         */
        fun createAssistantMessage(content: String, isTyping: Boolean = false): ChatMessage {
            return ChatMessage(
                id = generateId(),
                content = content,
                role = MessageRole.ASSISTANT,
                isTyping = isTyping
            )
        }

        /**
         * Create a thinking message
         */
        fun createThinkingMessage(): ChatMessage {
            return ChatMessage(
                id = generateId(),
                content = "正在思考...",
                role = MessageRole.ASSISTANT,
                isTyping = true
            )
        }

        private fun generateId(): String {
            return "msg_${System.currentTimeMillis()}_${(0..999).random()}"
        }

        /**
         * Create mock conversation
         */
        fun createMockConversation(): List<ChatMessage> {
            return listOf(
                createUserMessage("去故宫怎么走？", MessageIntent.SEARCH_POI),
                createAssistantMessage("好的，为您规划到故宫的路线。故宫位于北京市中心，距离您约3.5公里。"),
                createUserMessage("开始导航", MessageIntent.NAVIGATE),
                createAssistantMessage("导航已开始。沿当前道路向东行驶500米，然后右转进入京藏高速。")
            )
        }
    }
}
