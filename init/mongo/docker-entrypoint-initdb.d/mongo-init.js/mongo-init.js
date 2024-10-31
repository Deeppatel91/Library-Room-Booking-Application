print('START');

db = db.getSiblingDB('booking-service');

db.createUser(
    {
        user: 'admin',
        pwd: 'password',
        roles: [{role: 'readWrite', db: 'product-service'}]
    }
);

db.createCollection('user');


print('END');