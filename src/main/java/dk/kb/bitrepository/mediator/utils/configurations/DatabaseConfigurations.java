package dk.kb.bitrepository.mediator.utils.configurations;

import static java.lang.String.format;

public final class DatabaseConfigurations {
    private String name;
    private String url;
    private int port;
    private String username;
    private String password;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return format("port: %s\n", port) +
                format("name: %s\n", name) +
                format("username: %s\n", username) +
                format("password: %s\n", password) +
                format("url: %s\n", url);
    }
}
