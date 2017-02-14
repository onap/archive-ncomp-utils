
/*-
 * ============LICENSE_START==========================================
 * OPENECOMP - DCAE
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */
	
package org.openecomp.ncomp.utils.cache;

public interface SimpleCacheMBean<Key,Value,Builder extends CacheBuilder<Key,Value>> {
	public int getMaxEntries();
	public void setMaxEntries (int maxsize);
	public long getMaxEntryAge();
	public void setMaxEntryAge (long age);
	public long getMaxMemSize();
	public void setMaxMemSize(long maxMemSize);

	public int getMaxPendingEntries();
	public void setMaxPendingEntries (int maxsize);
	public long getMaxPendingEntryAge();
	public void setMaxPendingEntryAge (long age);
	public long getMaxPendingMemSize();
	public void setMaxPendingMemSize(long maxMemSize);

	public int getMaxFailedEntries();
	public void setMaxFailedEntries(int maxsize);
	public int getNumFailedEntries();
	public String[] getFailedEntries();
	
	public int getBuildFailed();
	public int getValidateFailed();
	
	public void setNumThreads (int n);
	public int getNumThreads ();
	public int getActiveThreadCount ();
	
	public int getNumEntries();
	public int getNumPendingEntries();
	public int getNumPrefetchQueued();
	public int getNumBuildingPrefetch();
	public int getNumBuildingGet();
	public long getMemSize();
	public long getPendingMemSize();

	public void resetCounters();
	public void flushCache();
	
	public int getCacheQueries();
	public int getCacheHits();
	public int getCacheHitPrefetch();
	public int getCacheBuilt();
	public int getPrefetchQueries();
	public int getPrefetchHits();
	public int getPrefetchBuilt();

	public String[] getEntries();
	public String[] getPendingEntries();
	public String[] getPrefetchQueuedEntries();
	public String[] getBuildingPrefetchEntries();
	public String[] getBuildingGetEntries();

	public String[] getEntriesDetailed();
	public String[] getThreadNames ();
}
