const gameRatingDb = db.getSiblingDB('gamerating');

gameRatingDb.createUser({
  user: 'gamerating',
  pwd: 'pass',
  roles: [{ role: 'readWrite', db: 'gamerating' }],
});