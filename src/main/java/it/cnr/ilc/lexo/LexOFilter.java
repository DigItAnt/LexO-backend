package it.cnr.ilc.lexo;

import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.hibernate.HibernateError;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author andreabellandi
 */
@WebFilter(urlPatterns = {"/faces/*", "/service/*", "/servlet/*"})
public class LexOFilter implements Filter {

    static final Logger logger = LoggerFactory.getLogger(LexOFilter.class.getName());
    public static String CONTEXT;
    public static String VERSION;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        CONTEXT = filterConfig.getServletContext().getContextPath().substring(1);
        logger.info("init(): Context " + CONTEXT);
        VERSION = LexOProperties.getProperty("application.version");
        logger.info("init(): Version " + VERSION);
        File logFile = new File(filterConfig.getServletContext().getRealPath("/"));
        logger.info("init(): " + CONTEXT + " started");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        logger.debug("doFilter() Request URL: " + httpRequest.getRequestURL().toString());
       // RepositoryConnection graphDBConnection = GraphDbUtil.getConnection();
        try {
            Session hbs = HibernateUtil.getSession();
            if (null != hbs) {
                Transaction tx = hbs.beginTransaction();
                if (tx != null) {
                    hbs.enableFilter("status");
                 //   graphDBConnection.begin();
                    chain.doFilter(request, response);
//                    if (graphDBConnection.isActive()) {
//                        graphDBConnection.commit();
//                    }
                    if (tx.isActive()) {
                        tx.commit();
                    }
                } else {
                    logger.error("Hibernate Transaction is null");
                    throw new HibernateError("Hibernate Transaction is null");
                }
            } else {
                logger.error("Hibernate Session is null");
                throw new HibernateError("Hibernate Session is null");
            }
        } catch (HibernateError ex) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            logger.error("Type of Exception: " + ex.getClass());
            /*
            if (ex instanceof org.hibernate.exception.GenericJDBCException) {
                logger.error("doFilter() Error connecting MySQL", ex);
                httpResponse.addHeader("Error", "Unable to connect to MySQL");
                httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to connect to MySQL");
            } else {
                httpResponse.addHeader("Error", "Unable to connect to ???");
                logger.error("doFilter()", ex);
                httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to connect to ???");
            }
             */
//            try {
//                if (graphDBConnection.isActive()) {
//                    graphDBConnection.rollback();
//                }
//            } catch (RepositoryException e) {
//                logger.error("doFilter(), Unable to connect to GraphDB", e);
//                httpResponse.addHeader("Error", "Unable to connect to GraphDB");
//                httpResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to connect to GraphDB");
//            }
            try {
                if (HibernateUtil.getSession().getTransaction().isActive()) {
                    HibernateUtil.getSession().getTransaction().rollback();
                }
            } catch (HibernateException e) {
                logger.error("doFilter()", e);
            }

        } finally {
//            try {
//                GraphDbUtil.releaseConnection(graphDBConnection);
//            } catch (Exception e) {
//                logger.error("doFilter()", e);
//            }
            try {
                HibernateUtil.getSession().close();
            } catch (HibernateException e) {
                logger.error("doFilter()", e);
            }
        }
        logger.debug("End of doFilter()");
    }

    @Override
    public void destroy() {
        // Logger.getLogger(CONTEXT).info(CONTEXT + " stop");
        logger.info("destroy() " + CONTEXT + " stop");
        try {
            HibernateUtil.closeFactory();
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage());
        }
        GraphDbUtil.shutDown();
    }

}
