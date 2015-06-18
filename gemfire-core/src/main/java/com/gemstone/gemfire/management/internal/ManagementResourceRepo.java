/*
 *  =========================================================================
 *  Copyright (c) 2002-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * more patents listed at http://www.pivotal.io/patents.
 *  ========================================================================
 */
package com.gemstone.gemfire.management.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Notification;
import javax.management.ObjectName;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.DistributedMember;

/**
 * Instance of this object behaves as a cache wide repository in the context of
 * management and monitoring. Various management infrastructures are initialized
 * by this class
 * 
 * Various repository related methods are declared here These methods provide a
 * consistent view to read and update the repository.
 * 
 * @author rishim
 * 
 */

public class ManagementResourceRepo {

  /**
   * Map containing member to Monitoring region mapping.
   */
  private Map<DistributedMember, Region<String, Object>> monitoringRegionMap;

  /**
   * Map containing member to Notification region mapping.
   */
  private Map<DistributedMember, Region<NotificationKey, Notification>> notifRegionMap;

  /**
   * local monitoring region
   */
  private Region<String, Object> localMonitoringRegion;

  /**
   * local notification region
   */
  private Region<NotificationKey, Notification> localNotificationRegion;

  public ManagementResourceRepo() {
    monitoringRegionMap = new ConcurrentHashMap<DistributedMember, Region<String, Object>>();
    notifRegionMap = new ConcurrentHashMap<DistributedMember, Region<NotificationKey, Notification>>();
  }

  /**
   * 
   * @return local monitoring region
   */
  public Region<String, Object> getLocalMonitoringRegion() {
    return localMonitoringRegion;
  }
  
  public void destroyLocalMonitoringRegion(){
    localMonitoringRegion.localDestroyRegion();
    localMonitoringRegion = null;
  }
  
  public void destroyLocalNotifRegion(){
    localNotificationRegion.localDestroyRegion();
    localNotificationRegion = null;
  }

  /**
   * Sets the repository local monitoring region to the given Region
   * 
   * @param localMonitoringRegion
   *          local monitoring region
   */
  public void setLocalMonitoringRegion(
      Region<String, Object> localMonitoringRegion) {
    this.localMonitoringRegion = localMonitoringRegion;
  }

  /**
   * put an entry in local monitoring region
   * 
   * @param name
   *          MBean name
   * @param data
   *          The value part of the Map
   */
  public void putEntryInLocalMonitoringRegion(String name, Object data) {
    if (localMonitoringRegion != null && !localMonitoringRegion.isDestroyed()) {
      localMonitoringRegion.put(name, data);
    }

  }

  /**
   * uses putAll operation of region
   * 
   * @param objectMap
   *          Object Map containing key-value operations
   */
  public void putAllInLocalMonitoringRegion(
      Map<String, FederationComponent> objectMap) {
    if (localMonitoringRegion != null && !localMonitoringRegion.isDestroyed()) {
      localMonitoringRegion.putAll(objectMap);
    }

  }
  
  public boolean keyExistsInLocalMonitoringRegion(String key) {
    if (localMonitoringRegion != null && !localMonitoringRegion.isDestroyed()) {
      // We want to just check locally without sending a message to the manager.
      // containsKey does this.
      return localMonitoringRegion.containsKey(key);
    } else {
      return true; // so caller will think he does not need to do a putAll
    }
  }


  /**
   * get a entry from local monitoring region
   * 
   * @param name
   *          MBean name
   * @return the value
   */
  public Object getEntryFromLocalMonitoringRegion(ObjectName name) {

    return localMonitoringRegion.get(name);
  }

  /**
   * 
   * @return local notification region
   */
  public Region<NotificationKey, Notification> getLocalNotificationRegion() {
    return localNotificationRegion;
  }

  /**
   * sets the local notification region
   * 
   * @param localNotificationRegion
   *          local notification region
   */
  public void setLocalNotificationRegion(
      Region<NotificationKey, Notification> localNotificationRegion) {
    this.localNotificationRegion = localNotificationRegion;
  }

  /**
   * put an entry in local notification region
   * 
   * @param key
   *          Notiofication key
   * @param notif
   *          Notification Object
   */
  public void putEntryInLocalNotificationRegion(NotificationKey key,
      Notification notif) {
    localNotificationRegion.put(key, notif);
  }

  /**
   * put an entry in Monitoring Region Map
   * 
   * @param member
   *          Distributed member
   * @param region
   *          Corresponding region
   */
  public void putEntryInMonitoringRegionMap(DistributedMember member,
      Region<String, Object> region) {
    monitoringRegionMap.put(member, region);
  }

  /**
   * 
   * @param member
   *          Distributed Member
   * @return the corresponding Monitoring region at Managing Node side
   */
  public Region<String, Object> getEntryFromMonitoringRegionMap(
      DistributedMember member) {

    return monitoringRegionMap.get(member);
  }

  /**
   * remove the entry corresponding to the distributed member
   * 
   * @param member
   *          Distributed Member
   */
  public void romoveEntryFromMonitoringRegionMap(DistributedMember member) {

    monitoringRegionMap.remove(member);
  }

  /**
   * 
   * @return the map containing all the member and region map
   */
  public Map<DistributedMember, Region<String, Object>> getMonitoringRegionMap() {
    return monitoringRegionMap;
  }

  /**
   * put an entry into notification region map
   * 
   * @param member
   *          Distributed Member
   * @param region
   *          Corresponding notification region
   */
  public void putEntryInNotifRegionMap(DistributedMember member,
      Region<NotificationKey, Notification> region) {
    notifRegionMap.put(member, region);
  }

  /**
   * get the notification region for a corresponding member
   * 
   * @param member
   *          Distributed Member
   * @return notification Region for the member
   */
  public Region<NotificationKey, Notification> getEntryFromNotifRegionMap(
      DistributedMember member) {

    return notifRegionMap.get(member);
  }

  /**
   * removes an entry from notification region
   * 
   * @param member
   *          Distributed Member
   */
  public void removeEntryFromNotifRegionMap(DistributedMember member) {

    notifRegionMap.remove(member);
  }


}