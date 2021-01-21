ALTER TABLE users DROP CONSTRAINT FK_users_region_id;
ALTER TABLE inventory_items DROP CONSTRAINT inventoryitemsitem;
ALTER TABLE items DROP CONSTRAINT items_category_id;
ALTER TABLE items DROP CONSTRAINT FK_items_seller_id;
ALTER TABLE bids DROP CONSTRAINT FK_bids_item_id;
ALTER TABLE bids DROP CONSTRAINT FK_bids_user_id;
ALTER TABLE comments DROP CONSTRAINT commentsto_user_id;
ALTER TABLE comments DROP CONSTRAINT comments_item_id;
ALTER TABLE comments DROP CONSTRAINT commentsfromuserid;
ALTER TABLE buy_now DROP CONSTRAINT FK_buy_now_item_id;
ALTER TABLE buy_now DROP CONSTRAINT buy_now_buyer_id;
DROP TABLE users;
DROP TABLE inventory_items;
DROP TABLE items;
DROP TABLE categories;
DROP TABLE bids;
DROP TABLE regions;
DROP TABLE comments;
DROP TABLE buy_now;
DELETE FROM SEQUENCE WHERE SEQ_NAME = 'SEQ_GEN';
DROP TABLE SEQUENCE;