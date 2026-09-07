package io.realworld.app.domain.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.realworld.app.domain.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.UUID

class DatabaseCompatibilityTest {
    @Test
    fun `create schema and persist users and tags on H2`() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:h2:mem:compatibility-${UUID.randomUUID()};DATABASE_TO_UPPER=false"
            username = "sa"
            password = ""
        }
        HikariDataSource(config).use { dataSource ->
            val database = Database.connect(dataSource)
            try {
                val users = UserRepository()
                val tags = TagRepository()
                val user = User(email = "database@example.com", username = "database-test", password = "password")
                val id = users.create(user)

                assertNotNull(id)
                assertEquals(id, users.findByEmail(user.email)?.id)
                assertEquals(user.email, users.findByUsername(user.username!!)?.email)
                assertEquals("Updated bio", users.update(user.email, user.copy(bio = "Updated bio"))?.bio)

                transaction(database) {
                    Tags.insert { it[name] = "kotlin" }
                }
                assertEquals(listOf("kotlin"), tags.findAll())
            } finally {
                TransactionManager.closeAndUnregister(database)
            }
        }
    }
}
