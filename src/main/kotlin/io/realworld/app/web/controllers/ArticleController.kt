package io.realworld.app.web.controllers

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.realworld.app.domain.ArticleDTO
import io.realworld.app.domain.ArticlesDTO
import io.realworld.app.domain.Article

class ArticleController(
    // The caller supplies article retrieval; HTTP tests use a fixed list.
    private val loadArticles: () -> List<Article> = { error("Article retrieval is not configured") }
) {
//class ArticleController(private val articleService: ArticleService) {

    fun findBy(ctx: ApplicationCall): ArticlesDTO {
        val tag = ctx.parameters["tag"]
        val author = ctx.parameters["author"]
        val favorited = ctx.parameters["favorited"]
        val limit = ctx.parameters["limit"] ?: "20"
        val offset = ctx.parameters["offset"] ?: "0"
//        articleService.findBy(tag, author, favorited, limit.toInt(), offset.toInt()).also { articles ->
//            ctx.json(ArticlesDTO(articles, articles.size))
//        }
        return ArticlesDTO(listOf(), 1)
    }

    fun feed(ctx: ApplicationCall): ArticlesDTO {
        val limit = ctx.parameters["limit"] ?: "20"
        val offset = ctx.parameters["offset"] ?: "0"
//        articleService.findFeed(ctx.attribute("email"), limit.toInt(), offset.toInt()).also { articles ->
//            ctx.json(ArticlesDTO(articles, articles.size))
//        }
        return ArticlesDTO(listOf(), 1)
    }

    fun get(ctx: ApplicationCall): ArticleDTO {
        ctx.parameters["slug"]
        //                articleService.findBySlug(slug).apply {
//                    ctx.json(ArticleDTO(this))
//                }
        return ArticleDTO(null)
    }

    suspend fun create(ctx: ApplicationCall): ArticleDTO {
        ctx.receive<ArticleDTO>()
        //            articleService.create(ctx.attribute("email"), article).apply {
//                ctx.json(ArticleDTO(this))
//            }
        return ArticleDTO(null)
    }

    suspend fun update(ctx: ApplicationCall): ArticleDTO {
        val slug = ctx.parameters["slug"]
        ctx.receive<ArticleDTO>()
        //            articleService.update(slug, article).apply {
//                ctx.json(ArticleDTO(this))
//            }
        return ArticleDTO(null)
    }

    fun delete(ctx: ApplicationCall) {
        ctx.parameters["slug"]
        //            articleService.delete(slug)
    }

    fun favorite(ctx: ApplicationCall): ArticleDTO {
        ctx.parameters["slug"]
        //            articleService.favorite(ctx.attribute("email"), slug).apply {
//                ctx.json(ArticleDTO(this))
//            }
        return ArticleDTO(null)
    }

    fun unfavorite(ctx: ApplicationCall): ArticleDTO {
        ctx.parameters["slug"]
        //            articleService.unfavorite(ctx.attribute("email"), slug).apply {
//                ctx.json(ArticleDTO(this))
//            }
        return ArticleDTO(null)
    }

    suspend fun popular(ctx: ApplicationCall) {
        val limit = paginationParameter(ctx.request.queryParameters["limit"], 20)
        val offset = paginationParameter(ctx.request.queryParameters["offset"], 0)
        if (limit == null || offset == null) {
            ctx.respond(
                HttpStatusCode.BadRequest,
                mapOf("errors" to mapOf("pagination" to listOf("limit and offset must be integers between 0 and 2147483647")))
            )
            return
        }

        ctx.respond(popular(loadArticles(), limit, offset))
    }

    private fun paginationParameter(value: String?, defaultValue: Int): Int? {
        return if (value == null) defaultValue else value.toIntOrNull()?.takeIf { it >= 0 }
    }

    fun popular(articles: List<Article>, limit: Int = 20, offset: Int = 0): ArticlesDTO {
        require(limit >= 0 && offset >= 0) { "limit and offset must be nonnegative" }
        val page = articles
            .sortedWith(compareByDescending<Article> { it.favoritesCount }.thenBy { it.slug })
            .drop(offset)
            .take(limit)
        return ArticlesDTO(page, articlesCount = articles.size)
    }
}
