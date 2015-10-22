package com.leidos.xchangecore.core.infrastructure.controller;

import java.lang.reflect.Method;

import javax.xml.stream.XMLStreamException;

import com.usersmarts.cx.Entity;
import com.usersmarts.cx.EntityType;
import com.usersmarts.cx.search.Results;
import com.usersmarts.geo.gml.GML2;
import com.usersmarts.geo.gml.GMLStaxFormatter;
import com.usersmarts.pub.atom.ATOM;
import com.usersmarts.pub.atom.OPENSEARCH;
import com.usersmarts.pub.georss.GEORSS;
import com.usersmarts.pub.georss.xml.GeoRSSFormatter;
import com.usersmarts.util.Coerce;
import com.usersmarts.util.stax.ExtendedXMLStreamWriter;
import com.usersmarts.xmf2.Configuration;
import com.usersmarts.xmf2.MarshalContext;
import com.usersmarts.xmf2.MarshalException;
import com.usersmarts.xmf2.stax.StaxAdaptersModule;
import com.usersmarts.xmf2.stax.StaxCompositeModule;
import com.usersmarts.xmf2.stax.StaxFormatter;
import com.usersmarts.xmf2.stax.StaxFormatterFinder;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class WorkProductModule
    extends StaxCompositeModule {

    public void configure(Configuration cfg) {

        super.configure(cfg);
        addFinder(new StaxFormatterFinder(new ResultsFormatter(), Results.class));
        cfg.require(StaxAdaptersModule.class);
    }

    private static class ResultsFormatter
        extends StaxFormatter {

        @Override
        public Object format(MarshalContext context, Object input, ExtendedXMLStreamWriter writer)
            throws MarshalException, XMLStreamException {

            Results<?> results = (Results<?>) input;

            writer.writeStartElement(ATOM.FEED);
            writer.element(ATOM.TITLE, "Results Feed");
            writer.element(OPENSEARCH.TOTAL_RESULTS, results.getResultSize());
            if (results.isPaging()) {
                writer.element(OPENSEARCH.ITEMS_PER_PAGE, results.getPageSize());
            }

            Envelope bbox = null;
            // marshal entries
            for (Object result : results) {
                if (result instanceof Entity) {
                    formatEntity(context, (Entity) result, writer);

                    Geometry geometry = (Geometry) ((Entity) result).getValue(GEORSS.WHERE);
                    if (geometry != null) {
                        Envelope env = geometry.getEnvelopeInternal();
                        if (bbox == null)
                            bbox = new Envelope(env.getMinX(),
                                                env.getMaxX(),
                                                env.getMinY(),
                                                env.getMaxY());
                        else
                            bbox.expandToInclude(env);
                    }
                } else {
                    context.marshal(result, writer);
                }
            }

            // marshal bbox of feed (aggregated bbox of all paged results)
            new GeoRSSFormatter().format(context, bbox, writer);

            writer.writeEndElement();
            return writer;
        }

        void formatEntity(MarshalContext context, Entity entity, ExtendedXMLStreamWriter out)
            throws XMLStreamException {

            EntityType entityType = entity.getEntityType();
            String type = entityType.getName().getLocalPart().toLowerCase();
            String id = entity.getId();
            String href = "api/" + type + "/" + id;
            Geometry geometry = (Geometry) entity.getValue(GEORSS.WHERE);

            out.element(ATOM.ENTRY);
            {
                out.element(ATOM.ID, type + "." + entity.getId());
                out.element(ATOM.TITLE, entity.getValue(ATOM.TITLE));
                out.element(ATOM.SUMMARY, entity.getValue(ATOM.SUMMARY));
                out.element(ATOM.PUBLISHED, entity.getValue(ATOM.PUBLISHED));
                out.element(ATOM.UPDATED, entity.getValue(ATOM.UPDATED));

                out.element(ATOM.CATEGORY);
                out.attribute("scheme", "http://uicds.org#types");
                out.attribute("term", type);
                out.end();

                out.element(ATOM.LINK);
                out.attribute("rel", "alternate");
                out.attribute("href", href);
                out.end();

                // include geometry
                if (geometry != null) {
                    out.element("gml", "where", GML2.NAMESPACE);
                    new GMLStaxFormatter().format(context, geometry, out);
                    out.end();
                }
            }
            out.end();
        }

        @SuppressWarnings("unused")
        protected String getId(Object value) {

            if (value == null)
                return null;
            Method[] methods = value.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if ("getId".equals(method.getName())) {
                    try {
                        return Coerce.toString(method.invoke(value, new Object[0]));
                    } catch (Exception e) {
                    }
                }
            }
            return null;
        }
    };
}
