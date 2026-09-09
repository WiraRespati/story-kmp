package com.learn.story.util

expect fun currentTimeMillis(): Long

fun generateDraftId(): String = "draft_${currentTimeMillis()}_${kotlin.random.Random.nextInt(1000, 9999)}"
