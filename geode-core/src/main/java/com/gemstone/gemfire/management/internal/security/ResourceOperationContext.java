/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gemstone.gemfire.management.internal.security;

import com.gemstone.gemfire.cache.operations.OperationContext;

/**
 * This is base class for OperationContext for resource (JMX and CLI) operations
 */
public class ResourceOperationContext extends OperationContext {

  private boolean isPostOperation = false;
  private Object opResult = null;

  // these default values are used when creating a lock around an operation
  private Resource resource = Resource.NULL;
  private OperationCode operation = OperationCode.NULL;
  private String regionName = OperationContext.ALL_REGIONS;

  public ResourceOperationContext() {
    this(null, null, null);
  }

  // When only specified a resource and operation, it's assumed that you need access to all regions in order to perform the operations
  // guarded by this ResourceOperationConext
  public ResourceOperationContext(String resource, String operation) {
    this(resource, operation, OperationContext.ALL_REGIONS);
  }

  public ResourceOperationContext(String resource, String operation, String regionName) {
    if (resource != null) this.resource = Resource.valueOf(resource);
    if (operation != null) this.operation = OperationCode.valueOf(operation);
    if (regionName !=null ) this.regionName = regionName;

    setParts(this.resource.name()+":"+this.operation.name()+":"+this.regionName, true);
  }

  @Override
  public boolean isClientUpdate() {
    return false;
  }

  @Override
  public OperationCode getOperationCode() {
    return operation;
  }

  @Override
  public Resource getResource() {
    return resource;
  }

  @Override
  public String getRegionName(){
    return this.regionName;
  }

  @Override
  public boolean isPostOperation() {
    return isPostOperation;
  }

  public void setPostOperationResult(Object result) {
    this.isPostOperation = true;
    this.opResult = result;
  }

  public Object getOperationResult() {
    return this.opResult;
  }
}