USE projetdev;

-- 1. Insert 8 Articles for Ngo (id=14)
-- We will use some dummy categories. Category 1: Environment, 2: Workshops, 3: Community.
-- Using a few image placeholders. Assuming no specific images, we can use empty or default ones.

INSERT INTO article (title, content, slug, created_at, published_at, created_by_id, writer_id, category_id, views, status, image) VALUES
('Global Beach Cleanup Initiative', 'We are organizing a global beach cleanup initiative starting next month. Join us to make a difference and save marine life.', 'global-beach-cleanup-initiative', '2026-03-27 10:00:00', '2026-03-27 10:00:00', 14, 14, 50, 150, 'published', 'cleanup.png'),
('Planting 10,000 Trees in Urban Areas', 'Our latest project aims to bring greenery back to the concrete jungle. We successfully secured funding to plant 10,000 trees across major cities.', 'planting-10000-trees-urban', '2026-03-31 14:30:00', '2026-03-31 14:30:00', 14, 14, 50, 230, 'published', 'trees.jpg'),
('Renewable Energy Workshop Recap', 'Thank you to everyone who attended our workshop on solar energy at home. It was a massive success with over 500 attendees.', 'renewable-energy-workshop-recap', '2026-04-05 09:15:00', '2026-04-05 09:15:00', 14, 14, 51, 410, 'published', 'solar.png'),
('Community Guidelines for Recycling', 'We have updated our comprehensive guide on how to sort your waste properly. Check out the community portal for more details.', 'community-guidelines-recycling', '2026-04-10 11:45:00', '2026-04-10 11:45:00', 14, 14, 52, 315, 'published', NULL),
('Partnering with Local Schools', 'Education is key. We are proud to announce our partnership with 20 local schools to integrate environmental studies into their curriculum.', 'partnering-local-schools', '2026-04-15 16:20:00', '2026-04-15 16:20:00', 14, 14, 52, 180, 'published', NULL),
('The Impact of Fast Fashion', 'A deep dive into how our clothing choices impact the environment, and what we can do to shop more sustainably.', 'impact-of-fast-fashion', '2026-04-20 08:00:00', '2026-04-20 08:00:00', 14, 14, 50, 550, 'published', NULL),
('Zero Waste Cooking Class', 'Join us next week for a zero-waste cooking class where we will teach you how to use every part of your vegetables.', 'zero-waste-cooking-class', '2026-04-24 13:10:00', '2026-04-24 13:10:00', 14, 14, 51, 95, 'published', NULL),
('Urgent Call for Climate Action', 'The time to act is now. Read our latest manifesto on why we need immediate policy changes to protect our ecosystems.', 'urgent-call-climate-action', '2026-04-27 09:00:00', '2026-04-27 09:00:00', 14, 14, 50, 50, 'published', NULL);

-- We need to get the IDs of the newly inserted articles.
-- Since they were inserted sequentially, we can use variables or subqueries to get their IDs.
-- Let's define variables for the article IDs by slug
SET @art1 = (SELECT id FROM article WHERE slug = 'global-beach-cleanup-initiative' LIMIT 1);
SET @art2 = (SELECT id FROM article WHERE slug = 'planting-10000-trees-urban' LIMIT 1);
SET @art3 = (SELECT id FROM article WHERE slug = 'renewable-energy-workshop-recap' LIMIT 1);
SET @art4 = (SELECT id FROM article WHERE slug = 'community-guidelines-recycling' LIMIT 1);
SET @art5 = (SELECT id FROM article WHERE slug = 'partnering-local-schools' LIMIT 1);
SET @art6 = (SELECT id FROM article WHERE slug = 'impact-of-fast-fashion' LIMIT 1);
SET @art7 = (SELECT id FROM article WHERE slug = 'zero-waste-cooking-class' LIMIT 1);
SET @art8 = (SELECT id FROM article WHERE slug = 'urgent-call-climate-action' LIMIT 1);

-- 2. Insert Comments
INSERT INTO comment (article_id, content, created_at, author_name) VALUES
(@art1, 'This is fantastic! I will definitely join the local chapter.', '2026-03-27 12:00:00', 'Jane Doe'),
(@art1, 'Where can we sign up?', '2026-03-28 09:30:00', 'John Smith'),
(@art2, 'Trees make such a big difference in the city heat.', '2026-04-01 10:00:00', 'Alice Eco'),
(@art3, 'The workshop was very informative. Thanks!', '2026-04-05 14:00:00', 'Bob Green'),
(@art3, 'Will there be a recording available?', '2026-04-06 08:00:00', 'Charlie Brown'),
(@art4, 'Finally, a clear guide on recycling plastics.', '2026-04-11 15:20:00', 'Diana Prince'),
(@art5, 'Kids need to learn this early on.', '2026-04-16 11:10:00', 'Eve Adams'),
(@art6, 'Fast fashion is terrible. I try to buy second hand now.', '2026-04-20 18:45:00', 'Frank Castle'),
(@art6, 'Great read. It really opened my eyes.', '2026-04-21 09:20:00', 'Grace Hopper'),
(@art7, 'Can not wait for this class!', '2026-04-25 10:00:00', 'Heidi Klum'),
(@art8, 'We need to keep pushing for change.', '2026-04-27 10:30:00', 'Ivan Drago');

-- 3. Insert Reactions
-- Using user_id = 2 (Wiem), user_id = 13 (Admin), user_id = 15 (User) for reactions
INSERT INTO article_reaction (article_id, user_id, type) VALUES
(@art1, 2, 'like'),
(@art1, 13, 'like'),
(@art2, 15, 'like'),
(@art3, 2, 'like'),
(@art3, 13, 'like'),
(@art4, 2, 'like'),
(@art5, 15, 'like'),
(@art6, 2, 'like'),
(@art6, 13, 'like'),
(@art6, 15, 'like'),
(@art7, 2, 'like'),
(@art8, 13, 'like'),
(@art8, 15, 'like');
