package ee.fujitsu.fooddelivery.utils;

import jakarta.persistence.EntityManager;

public class RepositoryTestUtils {
    public static void flushAndClear(EntityManager entityManager) {
        entityManager.flush(); 
        entityManager.clear(); 
    }
}
