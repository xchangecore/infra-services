package com.leidos.xchangecore.core.infrastructure.service;

import org.springframework.transaction.annotation.Transactional;
import org.uicds.resourceProfileService.Interest;
import org.uicds.resourceProfileService.ResourceProfile;
import org.uicds.resourceProfileService.ResourceProfileListType;

import com.leidos.xchangecore.core.infrastructure.model.ResourceProfileModel;
import com.saic.precis.x2009.x06.base.IdentifierType;

/**
 * The XchangeCore Resource Profile Management Service provides a means to create,
 * discover, and update information about XchangeCore Resource Profiles.
 * 
 * @author Andre Bonner
 * @since 1.0
 * @ssdd
 */
@Transactional
public interface ResourceProfileService {

    public static final String RESOURCEPROFILE_SERVICE_NAME = "ResourceProfileService";

    /**
     * Creates a new profile for a XchangeCore entity
     * 
     * @param request
     * @return the resource profile model
     * @ssdd
     */
    public ResourceProfileModel createProfile(ResourceProfile request);

    /**
     * Update a profile
     * @param request
     * @return the resource profile model
     * @ssdd
     */
    public ResourceProfileModel updateProfile(ResourceProfile request);

    /**
     * Deletes an existing profile
     * 
     * @param request
     * @ssdd
     */
    public void deleteProfile(IdentifierType profileID);

    /**
     * Returns the profile associated with a XchangeCore entity
     * 
     * @param profileID the profile id
     * 
     * @return the profile
     * @ssdd
     */
    public ResourceProfileModel getProfile(IdentifierType profileID);

    /**
     * Adds the interest.
     * 
     * @param profileID the profile id
     * @param interest the interest
     * 
     * @return true, if successful
     * @ssdd
     */
    public boolean addInterest(IdentifierType profileID, Interest interest);

    /**
     * Removes the interest.
     * 
     * @param profileID the profile id
     * @param interest the interest
     * 
     * @return true, if successful
     * @ssdd
     */
    public boolean removeInterest(IdentifierType profileID, Interest interest);

    /**
     * Returns a list of profiles whose ids partially match the supplied queryString.
     * 
     * @param queryString the query string
     * 
     * @return the profile list
     * @ssdd
     */
    public ResourceProfileListType getProfileList(String queryString);

    public void setDirectoryService(DirectoryService directoryService);

    /**
     * SystemIntialized Message Handler
     * 
     * @param message SystemInitialized message
     * @return void
     * @see applicationContext
     * @ssdd
     */
    public void systemInitializedHandler(String messgae);

}
