package org.usergrid.persistence;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.usergrid.AbstractCoreIT;

import static org.junit.Assert.assertEquals;


public class PathQueryIT extends AbstractCoreIT {

    @Test
    public void testUserDevicePathQuery() throws Exception {
        UUID applicationId = setup.createApplication( "testOrganization", "testUserDevicePathQuery" );
        EntityManager em = setup.getEmf().getEntityManager( applicationId );

        List<Entity> users = new ArrayList<Entity>();
        for ( int i = 0; i < 15; i++ ) {
            Map<String, Object> properties = new LinkedHashMap<String, Object>();
            properties.put( "index", i );
            properties.put( "username", "user " + i );
            Entity created = em.create( "user", properties );
            users.add( created );
        }

        List<UUID> deviceIds = new ArrayList<UUID>();
        for ( Entity user : users ) {
            for ( int i = 0; i < 5; i++ ) {
                Map<String, Object> properties = new LinkedHashMap<String, Object>();
                properties.put( "index", i );
                Entity created = em.create( "device", properties );
                deviceIds.add( created.getUuid() );
                em.addToCollection( user, "devices", created );
            }
        }

        // pick an arbitrary user, ensure it has 5 devices
        Results devices = em.getCollection( users.get( 10 ), "devices", null, 20, Results.Level.IDS, false );
        assertEquals( 5, devices.size() );

        int pageSize = 10; // shouldn't affect these tests

        Query userQuery = new Query();
        userQuery.setCollection( "users" );
        userQuery.setLimit( pageSize );
        userQuery.addFilter( "index >= 2" );
        userQuery.addFilter( "index <= 13" );
        int expectedUserQuerySize = 12;

        // query the users, ignoring page boundaries
        Results results = em.searchCollection( em.getApplicationRef(), "users", userQuery );
        PagingResultsIterator pri = new PagingResultsIterator( results );
        int count = 2;
        while ( pri.hasNext() ) {
            Entity e = ( Entity ) pri.next();
            assertEquals( count++, ( ( Long ) e.getProperty( "index" ) ).intValue() );
        }
        assertEquals( count, expectedUserQuerySize + 2 );

        // query devices as a sub-query of the users, ignoring page boundaries
        Query deviceQuery = new Query();
        deviceQuery.setCollection( "devices" );
        deviceQuery.setLimit( pageSize );
        deviceQuery.addFilter( "index >= 2" );
        int expectedDeviceQuerySize = 3;

        PathQuery<UUID> usersPQ = new PathQuery<UUID>( em.getApplicationRef(), userQuery );
        PathQuery<Entity> devicesPQ = usersPQ.chain( deviceQuery );
        HashSet set = new HashSet( expectedUserQuerySize * expectedDeviceQuerySize );
        Iterator<Entity> i = devicesPQ.iterator( em );
        while ( i.hasNext() ) {
            set.add( i.next() );
        }
        assertEquals( expectedUserQuerySize * expectedDeviceQuerySize, set.size() );
    }


    @Test
    public void testGroupUserDevicePathQuery() throws Exception {

        UUID applicationId = setup.createApplication( "testOrganization", "testGroupUserDevicePathQuery" );
        EntityManager em = setup.getEmf().getEntityManager( applicationId );

        List<Entity> groups = new ArrayList<Entity>();
        for ( int i = 0; i < 4; i++ ) {
            Map<String, Object> properties = new LinkedHashMap<String, Object>();
            properties.put( "index", i );
            properties.put( "path", "group_" + i );
            Entity created = em.create( "group", properties );
            groups.add( created );
        }

        List<Entity> users = new ArrayList<Entity>();
        for ( Entity group : groups ) {
            for ( int i = 0; i < 7; i++ ) {
                Map<String, Object> properties = new LinkedHashMap<String, Object>();
                properties.put( "index", i );
                properties.put( "username", group.getProperty( "path" ) + " user " + i );
                Entity created = em.create( "user", properties );
                em.addToCollection( group, "users", created );
                users.add( created );
            }
        }

        // pick an arbitrary group, ensure it has 7 users
        Results ru = em.getCollection( groups.get( 2 ), "users", null, 20, Results.Level.IDS, false );
        assertEquals( 7, ru.size() );

        List<UUID> devices = new ArrayList<UUID>();
        for ( Entity user : users ) {
            for ( int i = 0; i < 7; i++ ) {
                Map<String, Object> properties = new LinkedHashMap<String, Object>();
                properties.put( "index", i );
                Entity created = em.create( "device", properties );
                devices.add( created.getUuid() );
                em.addToCollection( user, "devices", created );
            }
        }

        // pick an arbitrary user, ensure it has 7 devices
        Results rd = em.getCollection( users.get( 6 ), "devices", null, 20, Results.Level.IDS, false );
        assertEquals( 7, rd.size() );

        int pageSize = 3; // ensure we're crossing page boundaries

        Query groupQuery = new Query();
        groupQuery.setCollection( "groups" );
        groupQuery.setLimit( pageSize );
        groupQuery.addFilter( "index <= 7" );
        int expectedGroupQuerySize = 4;

        Query userQuery = new Query();
        userQuery.setCollection( "users" );
        userQuery.setLimit( pageSize );
        userQuery.addFilter( "index >= 2" );
        userQuery.addFilter( "index <= 6" );
        int expectedUserQuerySize = 5;

        Query deviceQuery = new Query();
        deviceQuery.setCollection( "devices" );
        deviceQuery.setLimit( pageSize );
        deviceQuery.addFilter( "index >= 4" );
        int expectedDeviceQuerySize = 3;

        PathQuery groupsPQ = new PathQuery( em.getApplicationRef(), groupQuery );
        PathQuery usersPQ = groupsPQ.chain( userQuery );
        PathQuery<Entity> devicesPQ = usersPQ.chain( deviceQuery );

        HashSet set = new HashSet( expectedGroupQuerySize * expectedUserQuerySize * expectedDeviceQuerySize );
        Iterator<Entity> i = devicesPQ.iterator( em );
        while ( i.hasNext() ) {
            set.add( i.next() );
        }
        assertEquals( expectedGroupQuerySize * expectedUserQuerySize * expectedDeviceQuerySize, set.size() );
    }
}
