package com.leidos.xchangecore.core.infrastructure.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ExtendedMetadataTest {

    @Test
    public void testExtendedMetadata() {

        final String code = "code";
        final String codespace = "codespace";
        final String label = "label";
        final String value = "value";
        ExtendedMetadata em = new ExtendedMetadata();
        final Set<ExtendedMetadata> emSet = new HashSet<ExtendedMetadata>();

        em.setCode(code);
        em.setCodespace(codespace);
        em.setLabel(label);
        em.setValue(value);
        emSet.add(em);
        System.out.println("EM: " + em);
        System.out.println("Set contains " + emSet.size() + " entries");

        em = new ExtendedMetadata();
        em.setCode(code + ".1");
        em.setCodespace(codespace + ".1");
        em.setLabel(label + ".1");
        em.setValue(value + ".1");
        emSet.add(em);
        System.out.println("EM: " + em);
        System.out.println("Set contains " + emSet.size() + " entries");
        for (final ExtendedMetadata e : emSet)
            System.out.println("Save: " + e);
    }

}
