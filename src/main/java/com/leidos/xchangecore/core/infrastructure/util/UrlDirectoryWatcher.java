package com.leidos.xchangecore.core.infrastructure.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UrlDirectoryWatcher
 * 
 * Directory watcher with source obtained using servlet urls
 * 
 * @author Santhosh Amanchi - Image Matters, LLC
 * @created
 */
public class UrlDirectoryWatcher
    extends TimerTask {

    public enum Change {
        MODIFIED, ADDED, DELETED
    };

    private String directoryPath;

    private Pattern pattern;

    private Map<URL, Long> knownFiles = new HashMap<URL, Long>();

    private Listener listener;

    ServletConfig config;

    Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @param manager
     */
    public UrlDirectoryWatcher(ServletConfig config, String directoryPath, Listener listener) {

        this(config, directoryPath, listener, null, false);
    }

    /**
     * @param manager
     */
    public UrlDirectoryWatcher(ServletConfig config, String directoryPath, Listener listener,
                               String pattern, boolean notifyInInit) {

        this.directoryPath = directoryPath;
        this.listener = listener;
        if (pattern != null) {
            setPattern(pattern);
        }
        this.config = config;
        this.init(notifyInInit);
    }

    public final void setPattern(String pattern) {

        this.pattern = Pattern.compile(pattern);
    }

    public void run() {

        try {

            Set<String> dir = config.getServletContext().getResourcePaths(directoryPath);
            if (dir.isEmpty()) {
                return;
            }
            Iterator it = dir.iterator();
            Set<URL> checkedFiles = new HashSet<URL>();
            while (it.hasNext()) {
                String fileName = (String) it.next();
                if (pattern != null) {
                    Matcher m = pattern.matcher(fileName);
                    if (!m.matches()) {
                        continue;
                    }
                }
                URL url = null;
                try {
                    url = config.getServletContext().getResource(fileName);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                File file = new File(url.getFile());
                checkedFiles.add(url);
                Long knownModified = knownFiles.get(url);
                if (knownModified == null) {
                    // new file
                    knownFiles.put(url, file.lastModified());
                    listener.onChange(url, Change.ADDED);
                } else if (!knownModified.equals(file.lastModified())) {
                    // file modified
                    knownFiles.put(url, file.lastModified());
                    listener.onChange(url, Change.MODIFIED);
                }

            }

            Set<URL> deletedFiles = new HashSet<URL>();
            deletedFiles.addAll(this.knownFiles.keySet());
            deletedFiles.removeAll(checkedFiles);
            for (URL url : deletedFiles) {
                knownFiles.remove(url);
                listener.onChange(url, Change.DELETED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final void init(boolean notify) {

        Set<String> dir = config.getServletContext().getResourcePaths("/");
        Iterator it = dir.iterator();
        while (it.hasNext()) {
            String fileName = (String) it.next();
            if (pattern != null) {
                Matcher m = pattern.matcher(fileName);
                if (!m.matches()) {
                    continue;
                }
            }
            URL url = null;
            try {
                url = config.getServletContext().getResource(fileName);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            File file = new File(url.getFile());
            if (url != null)
                knownFiles.put(url, file.lastModified());
            if (notify) {
                listener.onChange(url, Change.ADDED);
            }
        }
    }

    public interface Listener {

        void onChange(URL url, Change change);
    }
}