package gg.jte.kotlin.benchmark;

public class WelcomePage extends Page {
    public WelcomePage(int visits) {
        super(visits);
    }

    @Override
    public String getTitle() {
        return "Welcome!";
    }

    @Override
    public String getDescription() {
        return "Welcome to the benchmark site.";
    }

    @Override
    public String getTemplate() {
        return "welcome.kte";
    }
}
