package com.leidos.xchangecore.core.infrastructure.controller;

import java.util.Date;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import com.usersmarts.cx.search.SimpleQueryBuilder;
import com.usersmarts.cx.search.SinceDateParser;
import com.usersmarts.cx.search.WhenDateParser;
import com.usersmarts.functor.filter.FilterConstants;
import com.usersmarts.functor.filter.FilterExpressionBuilder;
import com.usersmarts.functor.filter.IFilterExpressionBuilderExtension;
import com.usersmarts.functor.filter.PropertyName;
import com.usersmarts.pub.atom.ATOM;
import com.usersmarts.pub.atom.OPENSEARCH;
import com.usersmarts.util.Coerce;
import com.usersmarts.util.MutableNamespaceContext;

/**
 * QueryBuilder
 * 
 */
public class WorkProductQueryBuilder
    extends SimpleQueryBuilder {

    private static final QName PRODUCT_TYPE = new QName("productType");
    private static final QName PRODUCT_ID = new QName("productId");
    private static final QName WHEN = new QName("when");
    private static final QName SINCE = new QName("since");
    private static final QName TYPE = new QName("type");

    public WorkProductQueryBuilder() {

        super();
        setMaxResultsDefault(ALL_RESULTS);
        setDefaultSortOrder(false);

        getSortKeys().put("title", ATOM.TITLE);
        getSortKeys().put("updated", ATOM.UPDATED);
        getSortKeys().put("published", ATOM.PUBLISHED);
        getSortKeys().put("status", new QName("status"));
        getSortKeys().put("exactTitle", new QName("exactTitle")); // for lucene
        // sorting

        NamespaceContext ctx = new MutableNamespaceContext();
        getPropertyNames().put(WHEN, new PropertyName(ATOM.PUBLISHED));
        getPropertyNames().put(SINCE, new PropertyName(ATOM.PUBLISHED));

        // register parsers for parameters
        getParameterParsers().put(WHEN, new WhenDateParser());
        getParameterParsers().put(SINCE, new SinceDateParser());

        //
        // register builder extensions for parameters
        //

        // associated object id parameter
        IFilterExpressionBuilderExtension idExt = new IFilterExpressionBuilderExtension() {

            public FilterExpressionBuilder handle(PropertyName propertyName,
                                                  Object value,
                                                  FilterExpressionBuilder builder) {

                builder.startFunctionNode(FilterConstants.PROPERTY_IS_EQUAL_TO);
                builder.addPropertyNameNode(propertyName);
                builder.addConstantNode(Coerce.toInteger(value));
                builder.endNode();
                return builder;
            }
        };
        getExtensions().put(PRODUCT_ID, idExt);

        // when
        getExtensions().put(WHEN, new IFilterExpressionBuilderExtension() {

            public FilterExpressionBuilder handle(PropertyName propertyName,
                                                  Object value,
                                                  FilterExpressionBuilder builder) {

                Date[] dates = (Date[]) value;
                builder.startFunctionNode(FilterConstants.PROPERTY_IS_BETWEEN);
                builder.addPropertyNameNode(propertyName);
                builder.addConstantNode(dates[0]);
                builder.addConstantNode(dates[1]);
                builder.endNode();
                return builder;
            }
        });

        // type
        getExtensions().put(TYPE, new IFilterExpressionBuilderExtension() {

            public FilterExpressionBuilder handle(PropertyName propertyName,
                                                  Object value,
                                                  FilterExpressionBuilder builder) {

                return builder;
            }
        });

        // keywords
        getExtensions().put(OPENSEARCH.KEYWORDS, new IFilterExpressionBuilderExtension() {

            @SuppressWarnings("unchecked")
            public FilterExpressionBuilder handle(PropertyName propertyName,
                                                  Object value,
                                                  FilterExpressionBuilder builder) {

                List<String> keywords = (List<String>) value;
                if (!keywords.isEmpty())
                    builder.startOrNode();
                for (String keyword : keywords) {
                    builder.function(FilterConstants.PROPERTY_IS_LIKE, ATOM.TITLE, "*" + keyword +
                                                                                   "*");
                    builder.function(FilterConstants.PROPERTY_IS_LIKE, ATOM.SUMMARY, "*" + keyword +
                                                                                     "*");
                }
                if (!keywords.isEmpty())
                    builder.endNode();
                return builder;
            }
        });
    }
}
