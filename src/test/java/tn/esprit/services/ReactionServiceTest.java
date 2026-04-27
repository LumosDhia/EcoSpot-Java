package tn.esprit.services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReactionServiceTest {

    private static ReactionService reactionService;
    private static final int TEST_ARTICLE_ID = 99999;
    private static final int TEST_USER_ID = 88888;

    @BeforeAll
    public static void setUp() {
        reactionService = new ReactionService();
        // Clear any leftover data from previous failed runs
        cleanup();
    }

    @AfterAll
    public static void tearDown() {
        cleanup();
    }

    private static void cleanup() {
        // Logic to remove test reactions is handled via toggleReaction in tests,
        // but let's ensure the table is clean for these IDs.
        reactionService.toggleReaction(TEST_ARTICLE_ID, TEST_USER_ID, "like"); // Ensure it's there
        reactionService.toggleReaction(TEST_ARTICLE_ID, TEST_USER_ID, "like"); // Toggle off
    }

    @Test
    @Order(1)
    public void testInitiallyNoReaction() {
        String reaction = reactionService.getUserReaction(TEST_ARTICLE_ID, TEST_USER_ID);
        assertNull(reaction, "Should have no reaction initially");
        assertEquals(0, reactionService.getLikes(TEST_ARTICLE_ID));
        assertEquals(0, reactionService.getDislikes(TEST_ARTICLE_ID));
    }

    @Test
    @Order(2)
    public void testAddLike() {
        String result = reactionService.toggleReaction(TEST_ARTICLE_ID, TEST_USER_ID, "like");
        assertEquals("like", result);
        assertEquals("like", reactionService.getUserReaction(TEST_ARTICLE_ID, TEST_USER_ID));
        assertEquals(1, reactionService.getLikes(TEST_ARTICLE_ID));
        assertEquals(0, reactionService.getDislikes(TEST_ARTICLE_ID));
    }

    @Test
    @Order(3)
    public void testToggleOffLike() {
        // Current state is 'like'. Toggling 'like' again should remove it.
        String result = reactionService.toggleReaction(TEST_ARTICLE_ID, TEST_USER_ID, "like");
        assertNull(result, "Toggling the same reaction should remove it");
        assertNull(reactionService.getUserReaction(TEST_ARTICLE_ID, TEST_USER_ID));
        assertEquals(0, reactionService.getLikes(TEST_ARTICLE_ID));
    }

    @Test
    @Order(4)
    public void testSwitchLikeToDislike() {
        // Add like first
        reactionService.toggleReaction(TEST_ARTICLE_ID, TEST_USER_ID, "like");
        
        // Toggle dislike
        String result = reactionService.toggleReaction(TEST_ARTICLE_ID, TEST_USER_ID, "dislike");
        assertEquals("dislike", result, "Should switch from like to dislike");
        assertEquals("dislike", reactionService.getUserReaction(TEST_ARTICLE_ID, TEST_USER_ID));
        assertEquals(0, reactionService.getLikes(TEST_ARTICLE_ID));
        assertEquals(1, reactionService.getDislikes(TEST_ARTICLE_ID));
    }

    @Test
    @Order(5)
    public void testMultipleUsers() {
        int anotherUser = 77777;
        reactionService.toggleReaction(TEST_ARTICLE_ID, anotherUser, "like");
        
        // TEST_USER_ID has 'dislike' from previous test
        assertEquals(1, reactionService.getLikes(TEST_ARTICLE_ID));
        assertEquals(1, reactionService.getDislikes(TEST_ARTICLE_ID));
        
        // Cleanup anotherUser
        reactionService.toggleReaction(TEST_ARTICLE_ID, anotherUser, "like");
    }
}
