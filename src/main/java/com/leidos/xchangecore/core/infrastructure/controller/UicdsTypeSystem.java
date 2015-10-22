package com.leidos.xchangecore.core.infrastructure.controller;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.usersmarts.cx.hbm.search.lucene.TypeSystem;
import com.usersmarts.functor.filter.PropertyName;
import com.usersmarts.pub.atom.ATOM;
import com.usersmarts.util.StringUtils;

public class UicdsTypeSystem
    implements TypeSystem {

    static Map<QName, String> fields = new HashMap<QName, String>();
    static Map<String, Type> types = new HashMap<String, Type>();

    static {
        types.put("id", Type.Text);
        types.put("owner", Type.Text);
        types.put("administrators", Type.Text);
        types.put("users", Type.Text);
        types.put("groups", Type.Text);
        types.put("project", Type.Text);
        types.put("account", Type.Text);
        types.put("accountPath", Type.Text);
        types.put("updated", Type.Date);
        types.put("lastModified", Type.Date);
        types.put("published", Type.Date);
        types.put("created", Type.Date);
        types.put("expires", Type.Date);
        types.put("expirationDate", Type.Date);
        types.put("userQuota", Type.Int);
        types.put("mayContainSubAccounts", Type.Bool);
        types.put("exactTitle", Type.String);

        fields.put(ATOM.ID, "id");
        fields.put(ATOM.AUTHOR, "owner");
        fields.put(ATOM.TITLE, "title");
        fields.put(ATOM.SUMMARY, "summary");
        fields.put(ATOM.PUBLISHED, "created");
        fields.put(ATOM.UPDATED, "lastModified");
        fields.put(new QName("exactTitle"), "exactTitle");
    }

    @Override
    public String getField(PropertyName propertyName) {

        QName part = propertyName.getParsedPath().get(0);
        String field = fields.get(part);
        if (StringUtils.isEmpty(field)) {
            String ns = part.getNamespaceURI();
            if (StringUtils.isNotEmpty(ns)) {
                field = ns + "/" + part.getLocalPart();
            } else {
                field = part.getLocalPart();
            }
        }
        return field;
    }

    @Override
    public Type getType(PropertyName name) {

        String fieldName = getField(name);
        Type result = types.get(fieldName);
        if (result == null) {
            result = Type.Text;
        }
        return result;
    }

}
