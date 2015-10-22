package com.leidos.xchangecore.core.infrastructure.interfaces;

import org.uicds.workProductService.WorkProductPublicationResponseDocument;

import com.saic.precis.x2009.x06.structures.WorkProductDocument.WorkProduct;

/**
 * 
 * @author bonnerad
 * 
 * @param <T>
 */
public interface HasDigest<T> {

    public WorkProduct getDigest(T value);

    public WorkProductPublicationResponseDocument getDigestList(T[] value);

}
