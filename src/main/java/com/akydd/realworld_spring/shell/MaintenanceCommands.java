package com.akydd.realworld_spring.shell;

import com.akydd.realworld_spring.service.ArticleService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

/**
 * Spring Shell maintenance commands, run non-interactively as one-off tasks, e.g.
 * {@code java -jar app.jar reconcile-counts --spring.main.web-application-type=none}.
 * The interactive shell is disabled (see application.properties) so a normal web launch is unaffected.
 */
@ShellComponent
public class MaintenanceCommands {

    private final ArticleService articleService;

    public MaintenanceCommands(ArticleService articleService) {
        this.articleService = articleService;
    }

    @ShellMethod(key = "reconcile-counts",
            value = "Rebuild every article's favoritesCount from the article_favorites source of truth.")
    public String reconcileCounts() {
        int updated = articleService.reconcileFavoritesCounts();
        return "Reconciled favoritesCount for " + updated + " article(s).";
    }
}
