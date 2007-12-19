package com.openexchange.webdav.protocol;

import java.util.*;

public class WebdavPath implements Iterable<String>{

    private List<String> components = new ArrayList<String>();

    public WebdavPath(CharSequence path) {
        StringBuilder component = new StringBuilder();
        for(int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if(c == '/') {
                if(component.length() > 0) {
                    components.add(component.toString());
                }
                component.setLength(0);
            } else {
                component.append(c);
            }
        }
        if(component.length() > 0) {
            components.add(component.toString());
        }
    }

    public WebdavPath(String...components) {
        append(components);
    }

    public WebdavPath(Collection<String> components) {
        append(components);
    }

    public Iterator<String> iterator() {
        return components.iterator();
    }

    public int size() {
        return components.size();
    }

    public WebdavPath append(String...components) {
        return append(Arrays.asList(components));
    }

    public WebdavPath append(Collection<String> strings) {
        this.components.addAll(strings);
        return this;
    }

    public WebdavPath append(WebdavPath webdavPath) {
        return append(webdavPath.components);
    }

    public WebdavPath parent(){
        if(components.size()<2) {
            return new WebdavPath();
        }
        return new WebdavPath(components.subList(0,components.size()-1));
    }

    public String name(){
        if(components.size() == 0)
            return "";
        return  components.get(components.size()-1);
    }

    public WebdavPath dup(){
        return new WebdavPath(components);
    }


    public WebdavPath subpath(int from){
        return subpath(from, size());
    }

    public WebdavPath subpath(int from, int to) {
        return new WebdavPath(components.subList(from,to));
    }

    public boolean equals(Object other) {
        if (!(other instanceof WebdavPath)) {
            return false;
        }
        return components.equals(((WebdavPath)other).components);
    }

    public boolean startsWith(WebdavPath path) {
        if(path.size() > size()) {
            return false;
        }

        for(int i = 0; i < path.size(); i++) {
            if(!components.get(i).equals(path.components.get(i))){
                return false;
            }
        }
        return true;
    }

    public int hashCode(){
        return components.hashCode();
    }

    public String toString(){
        StringBuilder b = new StringBuilder("/");
        for(String component : components) { b.append(component).append("/"); }
        b.setLength(b.length()-1);
        return b.toString();
    }

    public String toEscapedString() {
        StringBuilder b = new StringBuilder("/");
        for(String component : components) { b.append(_escape(component)).append("/"); }
        b.setLength(b.length()-1);
        return b.toString();
    }

    private String _escape(String component) {
        if(!component.contains("/") && !component.contains("\\")) {
            return component;
        }
        return component.replaceAll("\\\\","\\\\\\\\").replaceAll("/","\\\\/");
    }

}