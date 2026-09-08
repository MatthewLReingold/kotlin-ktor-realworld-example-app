package io.realworld.app.web.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.basic
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.server.testing.TestApplicationResponse
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.realworld.app.domain.Article
import io.realworld.app.domain.ArticleDTO
import io.realworld.app.domain.ArticlesDTO
import io.realworld.app.domain.ProfileDTO
import io.realworld.app.domain.User
import io.realworld.app.web.articles
import io.realworld.app.web.rules.AppRule
import io.realworld.app.web.util.HttpUtil
import org.apache.http.HttpStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.util.Date

@Ignore
class ArticleControllerTest {
    @Rule
    @JvmField
    val appRule = AppRule()

    @Test
    fun `get all articles`() {
        appRule.http.createArticle()
        val http = HttpUtil(appRule.port)
        val response = http.get<ArticlesDTO>("/api/articles")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.articles)
        assertEquals(response.body.articles.size, response.body.articlesCount)
    }

    @Test
    fun `get all articles with auth`() {
        appRule.http.createArticle()
        val response = appRule.http.get<ArticlesDTO>("/api/articles")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.articles)
        assertEquals(response.body.articles.size, response.body.articlesCount)
        assertNotNull(response.body.articles.first())
        assertFalse(response.body.articles.first().title.isNullOrBlank())
        assertTrue(response.body.articles.first().tagList.isNotEmpty())
    }

    @Test
    fun `get all articles by author`() {
        val author = "user_name_test"
        val response = appRule.http.get<ArticlesDTO>("/api/articles?author=$author")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.articles)
        assertEquals(response.body.articles.size, response.body.articlesCount)
        response.body.articles.forEach {
            assertEquals(it.author?.username, author)
            assertFalse(it.title.isNullOrBlank())
            assertTrue(it.tagList.isNotEmpty())
        }
    }

    @Test
    fun `get all articles by author with auth`() {
        appRule.http.createArticle()
        val author = "user_name_test"
        val response = appRule.http.get<ArticlesDTO>("/api/articles?author=$author")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.articles)
        assertEquals(response.body.articles.size, response.body.articlesCount)
        response.body.articles.forEach {
            assertEquals(it.author?.username, author)
            assertFalse(it.title.isNullOrBlank())
            assertTrue(it.tagList.isNotEmpty())
        }
    }

    @Test
    fun `get all articles favorited by username`() {
        val responseCreate = appRule.http.createArticle()
        appRule.http.post<ArticleDTO>("/api/articles/${responseCreate.body.article?.slug}/favorite")

        val response = appRule.http.get<ArticlesDTO>("/api/articles?favorited=user_name_test")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.articles)
        assertEquals(response.body.articles.size, response.body.articlesCount)
        assertNotNull(response.body.articles.first())
        assertFalse(response.body.articles.first().title.isNullOrBlank())
        assertTrue(response.body.articles.first().tagList.isNotEmpty())
        assertTrue(response.body.articles.first().favorited)
        assertTrue(response.body.articles.first().favoritesCount > 0)
    }

    @Test
    fun `get all articles favorited by username with auth`() {
        val responseCreate = appRule.http.createArticle()
        appRule.http.post<ArticleDTO>("/api/articles/${responseCreate.body.article?.slug}/favorite")

        val response = appRule.http.get<ArticlesDTO>("/api/articles?favorited=${responseCreate.body.article?.author?.username}")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.articles)
        assertEquals(response.body.articles.size, response.body.articlesCount)
        assertNotNull(response.body.articles.first())
        assertFalse(response.body.articles.first().title.isNullOrBlank())
        assertTrue(response.body.articles.first().tagList.isNotEmpty())
    }

    @Test
    fun `get all articles by tag`() {
        val responseCreate = appRule.http.createArticle()
        val tag = responseCreate.body.article?.tagList?.first()
        val response = appRule.http.get<ArticlesDTO>("/api/articles?tag=${responseCreate.body.article?.tagList?.first()}")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.articles)
        assertEquals(response.body.articles.size, response.body.articlesCount)
        assertTrue(response.body.articles.first().tagList.contains(tag))
    }

    @Test
    fun `create article`() {
        val article = Article(
            title = "Create How to train your dragon",
            description = "Ever wonder how?",
            body = "Very carefully.",
            tagList = listOf("create_article")
        )
        val response = appRule.http.createArticle(article)
        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.article)
        assertEquals(response.body.article?.title, article.title)
        assertEquals(response.body.article?.description, article.description)
        assertEquals(response.body.article?.body, article.body)
        assertEquals(response.body.article?.tagList, article.tagList)
    }

    @Test
    fun `get all articles of feed`() {
        appRule.http.createArticle()

        val http = HttpUtil(appRule.port)
        http.createUser("celeb_follow_profile@valid_email.com", "celeb_username")

        http.post<ProfileDTO>("/api/profiles/user_name_test/follow")

        val response = appRule.http.get<ArticlesDTO>("/api/articles/feed")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.articles)
        assertEquals(response.body.articles.size, response.body.articlesCount)
        assertNotNull(response.body.articles.first())
        assertFalse(response.body.articles.first().title.isNullOrBlank())
        assertTrue(response.body.articles.first().tagList.isNotEmpty())
    }

    @Test
    fun `get single article by slug`() {
        val responseArticle = appRule.http.createArticle()
        val slug = responseArticle.body.article?.slug
        val response = appRule.http.get<ArticleDTO>("/api/articles/$slug")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.article)
        assertNotNull(response.body.article?.body)
        assertFalse(response.body.article?.title.isNullOrBlank())
        assertNotNull(response.body.article?.description)
        assertTrue(response.body.article?.tagList?.isNotEmpty() ?: false)
    }

    @Test
    fun `update article by slug`() {
        val responseCreated = appRule.http.createArticle()
        val slug = responseCreated.body.article?.slug
        val article = Article(body = "Very carefully.", title = "Teste", description = "Teste Desc")
        val response = appRule.http.put<ArticleDTO>("/api/articles/$slug", ArticleDTO(article))

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.article)
        assertEquals(response.body.article?.body, article.body)
        assertNotNull(response.body.article?.body)
        assertFalse(response.body.article?.title.isNullOrBlank())
        assertNotNull(response.body.article?.description)
        assertTrue(response.body.article?.tagList?.isNotEmpty() ?: false)
    }

    @Test
    fun `favorite article by slug`() {
        val article = Article(
            title = "slug test",
            description = "Ever wonder how?",
            body = "Very carefully.",
            tagList = listOf("favorite")
        )
        appRule.http.createArticle(article)
        val slug = "slug-test"
        val response = appRule.http.post<ArticleDTO>("/api/articles/$slug/favorite")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.article)
        assertNotNull(response.body.article?.body)
        assertFalse(response.body.article?.title.isNullOrBlank())
        assertNotNull(response.body.article?.description)
        assertTrue(response.body.article?.tagList?.isNotEmpty() ?: false)
    }

    @Test
    fun `unfavorite article by slug`() {
        val email = "unfavorite_article@valid_email.com"
        val password = "Test"
        appRule.http.registerUser(email, password, "user_name_test")
        appRule.http.loginAndSetTokenHeader(email, password)
        val article = Article(
            title = "slug test 2",
            description = "Ever wonder how?",
            body = "Very carefully.",
            tagList = listOf("unfavorite")
        )
        appRule.http.post<ArticleDTO>("/api/articles", ArticleDTO(article))
        val slug = "slug-test-2"
        val response = appRule.http.deleteWithResponseBody<ArticleDTO>("/api/articles/$slug/favorite")

        assertEquals(response.status, HttpStatus.SC_OK)
        assertNotNull(response.body.article)
        assertNotNull(response.body.article?.body)
        assertFalse(response.body.article?.title.isNullOrBlank())
        assertNotNull(response.body.article?.description)
        assertTrue(response.body.article?.tagList?.isNotEmpty() ?: false)
    }

    @Test
    fun `delete article by slug`() {
        val responseCreate = appRule.http.createArticle()
        val response = appRule.http.delete("/api/articles/${responseCreate.body.article?.slug}")

        assertEquals(response.status, HttpStatus.SC_OK)
    }
}

class PopularArticlesHttpTest {
    private val mapper = jacksonObjectMapper()
    private val alpha = article("alpha", 0, "alice")
    private val bravo = article("bravo", 10, "bob")
    private val charlie = article("charlie", 2, "carol")
    private val delta = article("delta", 10, "dana")
    private val echo = article("echo", 5, "erin")
    private val unsortedArticles = listOf(charlie, delta, alpha, bravo, echo)

    @Test
    fun `public popular route sorts before pagination and preserves total count`() {
        // No Authorization header: the real router must expose this endpoint publicly.
        assertPage("limit=2&offset=1", ArticlesDTO(listOf(delta, echo), 5))
    }

    @Test
    fun `public popular route includes all authors in popularity order`() {
        assertPage("limit=10&offset=0", ArticlesDTO(listOf(bravo, delta, echo, charlie, alpha), 5))
    }

    @Test
    fun `zero limit is accepted and preserves total count`() {
        assertPage("limit=0&offset=0", ArticlesDTO(emptyList(), 5))
    }

    @Test
    fun `largest Int limit is accepted`() {
        assertPage("limit=2147483647&offset=0", ArticlesDTO(listOf(bravo, delta, echo, charlie, alpha), 5))
    }

    @Test
    fun `largest Int offset is accepted and preserves total count`() {
        assertPage("limit=2&offset=2147483647", ArticlesDTO(emptyList(), 5))
    }

    @Test
    fun `negative limit returns bad request`() = assertBadRequest("limit=-1&offset=0")

    @Test
    fun `fractional limit returns bad request`() = assertBadRequest("limit=1.5&offset=0")

    @Test
    fun `whole number with decimal point limit returns bad request`() = assertBadRequest("limit=3.0&offset=0")

    @Test
    fun `nonnumeric limit returns bad request`() = assertBadRequest("limit=abc&offset=0")

    @Test
    fun `empty supplied limit returns bad request`() = assertBadRequest("limit=&offset=0")

    @Test
    fun `overflowing limit returns bad request`() = assertBadRequest("limit=2147483648&offset=0")

    @Test
    fun `negative offset returns bad request`() = assertBadRequest("limit=2&offset=-1")

    @Test
    fun `fractional offset returns bad request`() = assertBadRequest("limit=2&offset=1.5")

    @Test
    fun `whole number with decimal point offset returns bad request`() = assertBadRequest("limit=2&offset=3.0")

    @Test
    fun `nonnumeric offset returns bad request`() = assertBadRequest("limit=2&offset=abc")

    @Test
    fun `empty supplied offset returns bad request`() = assertBadRequest("limit=2&offset=")

    @Test
    fun `overflowing offset returns bad request`() = assertBadRequest("limit=2&offset=2147483648")

    private fun assertPage(query: String, expected: ArticlesDTO) {
        request(query) { response ->
            assertEquals(HttpStatusCode.OK, response.status())
            val contentType = response.headers[HttpHeaders.ContentType]
            assertNotNull("Expected a JSON Content-Type", contentType)
            assertEquals(ContentType.Application.Json, ContentType.parse(contentType!!).withoutParameters())
            assertNotNull("Expected a JSON response body", response.content)
            // Tree equality checks every field, including unexpected or missing fields,
            // while allowing JSON object properties to appear in any order.
            assertEquals(mapper.readTree(mapper.writeValueAsString(expected)), mapper.readTree(response.content!!))
        }
    }

    private fun assertBadRequest(query: String) {
        request(query) { response ->
            assertEquals("Query: $query", HttpStatusCode.BadRequest, response.status())
        }
    }

    private fun request(query: String, check: (TestApplicationResponse) -> Unit) {
        withTestApplication({
            install(ContentNegotiation) { jackson {} }
            install(Authentication) {
                // Reject authentication so accidental nesting inside authenticate fails.
                basic { validate { null } }
            }
            install(Routing) {
                articles(ArticleController(loadArticles = { unsortedArticles }), CommentController())
            }
        }) {
            // Use the production route registration, with no database or registration setup.
            val call = handleRequest(HttpMethod.Get, "/articles/feed/popular?$query") {
                addHeader(HttpHeaders.Accept, ContentType.Application.Json.toString())
            }
            check(call.response)
        }
    }

    private fun article(slug: String, favoritesCount: Long, author: String) = Article(
        slug = slug,
        title = "Article $slug",
        description = "Description for $slug",
        body = "Body for $slug",
        tagList = listOf("popular", slug),
        createdAt = Date(1_700_000_000_000L),
        updatedAt = Date(1_700_000_060_000L),
        favorited = false,
        favoritesCount = favoritesCount,
        author = User(email = "$author@example.com", username = author, bio = "Bio for $author", image = "image-$author")
    )
}

class PopularArticlesTest {
    // Test fixtures: ordinary Article objects, independent of the database and HTTP setup.
    private val emptyArticles = emptyList<Article>()

    // Bravo and delta tie: slug ascending should place bravo before delta.
    private val unsortedArticles = listOf(
        article("charlie", 2),
        article("delta", 10),
        article("alpha", 0),
        article("bravo", 10),
        article("echo", 5)
    )

    private val shortArticles = listOf(
        article("short-a", 3),
        article("short-b", 9),
        article("short-c", 6)
    )

    private val longArticles = listOf(
        article("long-a", 4),
        article("long-b", 16),
        article("long-c", 0),
        article("long-d", 8),
        article("long-e", 2),
        article("long-f", 14),
        article("long-g", 6),
        article("long-h", 12)
    )

    @Test
    fun `empty articles returns an empty list and zero count`() {
        // Arrange: create the controller and describe the expected response.
        val controller = ArticleController()
        val expected = ArticlesDTO(articles = emptyList(), articlesCount = 0)

        // Act: call the planned popular function with our empty input fixture.
        val actual = controller.popular(articles = emptyArticles, limit = 20, offset = 0)

        // Assert: compare the complete response, including the list and count.
        assertEquals(expected, actual)
    }

    @Test
    fun `articles are ordered by favorites descending then slug ascending`() {
        // Arrange: specify the expected order explicitly, including the tie at 10 favorites.
        val controller = ArticleController()
        val expected = ArticlesDTO(
            articles = listOf(
                article("bravo", 10),
                article("delta", 10),
                article("echo", 5),
                article("charlie", 2),
                article("alpha", 0)
            ),
            articlesCount = 5
        )

        // Act: give the real function the unsorted input fixture.
        val actual = controller.popular(articles = unsortedArticles, limit = 20, offset = 0)

        // Assert: compare the complete response, including the list and count.
        assertEquals(expected, actual)
    }

    @Test
    fun `limit larger than the short list returns all articles sorted`() {
        // Arrange: expect all three articles in popularity order.
        val controller = ArticleController()
        val expected = ArticlesDTO(
            articles = listOf(
                article("short-b", 9),
                article("short-c", 6),
                article("short-a", 3)
            ),
            articlesCount = 3
        )

        // Act: request up to ten articles without skipping any.
        val actual = controller.popular(articles = shortArticles, limit = 10, offset = 0)

        // Assert: a limit larger than the list still returns exactly the available articles.
        assertEquals(expected, actual)
    }

    @Test
    fun `limit smaller than the long list returns limited articles sorted`() {
        // Arrange: expect top three articles in popularity order.
        val controller = ArticleController()
        val expected = ArticlesDTO(
            articles = listOf(
                article("long-b", 16),
                article("long-f", 14),
                article("long-h", 12)
            ),
            articlesCount = 8
        )

        // Act: request up to three articles without skipping any.
        val actual = controller.popular(articles = longArticles, limit = 3, offset = 0)

        // Assert: a limit smaller than the list returns exactly the top n articles.
        assertEquals(expected, actual)
    }

    @Test
    fun `limit is 0 returns empty list and preserves articlesCount`() {
        // Arrange: expect 0 articles with articlesCount = 8.
        val controller = ArticleController()
        val expected = ArticlesDTO(articles = emptyList(), articlesCount = 8)

        // Act: request 0 articles without skipping any.
        val actual = controller.popular(articles = longArticles, limit = 0, offset = 0)

        // Assert: a limit 0 returns 0 articles.
        assertEquals(expected, actual)
    }

    @Test
    fun `offset skips three sorted articles and limit returns the next four`() {
        // Arrange: expect positions 3 through 6 after sorting, counting from zero.
        val controller = ArticleController()
        val expected = ArticlesDTO(
            articles = listOf(
                article("long-d", 8),
                article("long-g", 6),
                article("long-a", 4),
                article("long-e", 2)
            ),
            articlesCount = 8
        )

        // Act: skip the top three articles, then request up to four.
        val actual = controller.popular(articles = longArticles, limit = 4, offset = 3)

        // Assert: return the correct page while retaining the total count.
        assertEquals(expected, actual)
    }

    @Test
    fun `offset leaves fewer articles than limit and returns the remaining articles`() {
        // Arrange: only positions 6 and 7 remain in the sorted list.
        val controller = ArticleController()
        val expected = ArticlesDTO(
            articles = listOf(
                article("long-e", 2),
                article("long-c", 0)
            ),
            articlesCount = 8
        )

        // Act: skip six articles and request up to three.
        val actual = controller.popular(articles = longArticles, limit = 3, offset = 6)

        // Assert: return the final two articles and the original total count.
        assertEquals(expected, actual)
    }

    @Test
    fun `offset beyond the list returns an empty list and preserves total count`() {
        val controller = ArticleController()
        val expected = ArticlesDTO(articles = emptyList(), articlesCount = 8)

        // Skip beyond all eight articles.
        val actual = controller.popular(articles = longArticles, limit = 3, offset = 10)

        assertEquals(expected, actual)
    }

    @Test
    fun `offset equals list size returns an empty list and preserves total count`() {
        val controller = ArticleController()
        val expected = ArticlesDTO(articles = emptyList(), articlesCount = 8)

        // Skip beyond all eight articles.
        val actual = controller.popular(articles = longArticles, limit = 3, offset = 8)

        assertEquals(expected, actual)
    }

    // Keep required fields out of each fixture so favorite counts are easy to see.
    private fun article(slug: String, favoritesCount: Long): Article {
        return Article(
            slug = slug,
            title = "Article $slug",
            description = "Description for $slug",
            body = "Body for $slug",
            favoritesCount = favoritesCount
        )
    }
}
