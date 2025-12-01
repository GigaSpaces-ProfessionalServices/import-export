package com.gigaspaces.tools.importexport.config;

import com.gigaspaces.tools.importexport.Constants;
import com.gigaspaces.tools.importexport.remoting.RouteTranslator;
//import com.google.common.base.Joiner;
import com.j_spaces.core.IJSpace;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import java.util.Collection;
//import org.openspaces.core.space.UrlSpaceConfigurer;

import java.util.List;
import java.util.logging.Logger;

//import static com.google.common.base.Strings.isNullOrEmpty;

public class SpaceConnectionFactory {

    private static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static final int LOOKUP_TIMEOUT = 2000;

    private ExportConfiguration config;

    private SpaceProxyConfigurer configurer;
    private GigaSpace space;
    private Admin admin;
    private RouteTranslator router;

    public SpaceConnectionFactory(ExportConfiguration config) {
        this.config = config;
    }

    public GigaSpace createProxy() {

        if(space == null) {
            configurer = new SpaceProxyConfigurer(config.getName());
            //urlSpaceConfigurer = new UrlSpaceConfigurer("jini://*/*/" + config.getName());
            if (!isNullOrEmpty(config.getLocators())) {
                configurer.lookupLocators(join(config.getLocators()));
            }
            if (! isNullOrEmpty(config.getGroups())) {
                configurer.lookupGroups(join(config.getGroups()));
            }

            if ((!isNullOrEmpty(config.getUsername()) && !isNullOrEmpty(config.getPassword())) && isSecuredComponent(SecurityLevel.SPACE)) {
                logger.info("Using secured connection.");
                configurer.credentials(config.getUsername(), config.getPassword());
            }

            configurer.lookupTimeout(LOOKUP_TIMEOUT);
            space = new GigaSpaceConfigurer(configurer).gigaSpace();

            logger.info("Creating connection with url: \n" + space.getSpace().getURL());
        }

        return space;
    }

    private boolean isSecuredComponent(SecurityLevel securityLevel) {
        SecurityLevel configLevel = config.getSecurity();
        return configLevel == SecurityLevel.BOTH || configLevel == securityLevel;
    }
    private boolean isNullOrEmpty(Object o) {
        if (o instanceof String) {
            String s = (String) o;
            if (s == null || s.isEmpty()) {
                return true;
            }
            else {
                return false;
            }
        }
        else if (o instanceof Collection) {
            Collection c = (Collection) o;
            if (c == null || c.size() == 0) {
                return true;
            }
            else {
                return false;
            }
        }
        throw new UnsupportedOperationException("This method only supports String and Collection types.");
    }
    private String join(List<String> args) {
        return String.join(",", args);
        //return Joiner.on(",").join(args);
    }

    public void close() throws Exception {
        if(configurer != null){
            configurer.close();
        }

        if(admin != null){
            admin.close();
        }
    }

    public Admin createAdmin() {
        if(admin == null){
            AdminFactory adminFactory = new AdminFactory();
            adminFactory.addGroups(join(config.getGroups()));
            adminFactory.addLocators(join(config.getLocators()));

            if ((!isNullOrEmpty(config.getUsername()) && !isNullOrEmpty(config.getPassword())) && isSecuredComponent(SecurityLevel.GRID)) {
                logger.info("Using secured admin connection.");
                adminFactory.credentials(config.getUsername(), config.getPassword());
            }

            admin = adminFactory.create();
        }

        return admin;
    }

    public RouteTranslator createRouter() {
        if(router == null){
            router = new RouteTranslator(config.getPartitions(), config.getNewPartitionCount(), createProxy());
            router.initialize();
        }
        return router;
    }
}
