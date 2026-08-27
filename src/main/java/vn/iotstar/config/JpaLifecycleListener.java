package vn.iotstar.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class JpaLifecycleListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JpaConfig.getFactory();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaConfig.close();
    }
}
