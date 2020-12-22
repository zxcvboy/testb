    using { z.sap.com.testb as db } from '../db/schema';

    // Define Books Service
    service ServiceB {
         entity EntityB as projection on db.entityB;
    }
