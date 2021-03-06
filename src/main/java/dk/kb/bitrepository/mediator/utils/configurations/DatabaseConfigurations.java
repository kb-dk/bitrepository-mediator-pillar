package dk.kb.bitrepository.mediator.utils.configurations;

import java.util.Locale;

import static java.lang.String.format;

public final class DatabaseConfigurations {
    private String url;
    private String username;
    private String password;

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return  format(Locale.ROOT,"username: %s\n", username) +
                format(Locale.ROOT,"password: %s\n", password) +
                format(Locale.ROOT,"url: %s\n", url);
    }
}
