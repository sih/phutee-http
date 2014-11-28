package net.phutee.http.api;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import net.phutee.data.api.PopulatorAPI;
import net.phutee.data.domain.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class TeamsServletTest {
    
    // expected ouputs
    private static final String HELP_TEXT = "{\r"+
"\"Usage\":\"GET \\\\teams\\\\all or GET \\\\teams\\\\division\\\\<division_name>\",\r\t"+
	"\"divisions\":[\"ENGLISH_PREMIERSHIP\",\"SCOTTISH_PREMIERSHIP\",\"SCOTTISH_CHAMPIONSHIP\",\"GERMAN_BUNDESLIGA\",\"FRENCH_LIGUE1\",\"SPANISH_PRIMERA\",\"DUTCH_EREDIVISE\",\"PORTUGUESE_LIGA\",\"SWISS_SUPER_LEAGUE\"]\r"+
"}";
    private static final String WHUFC = "{\"ENGLISH_PREMIERSHIP\":[{\"name\":\"West Ham\",\"played\":\"12\",\"goalDiff\":\"+4\",\"points\":\"18\",\"form\":\"3-2-1\"}]}";
    private static final String NO_TEAMS = "{\"ENGLISH_PREMIERSHIP\":[]}";
    
    // api results
    private static final Map<String,List<Object>> EMPTY_RESULTS = new HashMap<String,List<Object>>();
    private static final List<Object> EMPTY_LIST = new ArrayList<Object>();
    private Map<String,List<Object>> westHam;
    private List<Object> teams;
    
    
    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    PopulatorAPI api;

    @InjectMocks
    TeamsServlet servlet;

    // ouptuts
    Map<String, List<Object>> getAllResults;
    List<Object> getByDivisionResults;
    PrintWriter out;
    StringWriter writer;

    @Before
    public void setUp() throws Exception {
	writer = new StringWriter();
	out = new PrintWriter(writer);
	when(response.getWriter()).thenReturn(out);
	
	teams = new ArrayList<Object>();
	// {"name":"West Ham","played":"12","goalDiff":"+4","points":"18","form":"3-2-1"}
	Team whufc = new Team("West Ham");
	whufc.setPlayed("12");
	whufc.setGoalDiff("+4");
	whufc.setPoints("18");
	whufc.setForm("3-2-1");
	teams.add(whufc);

	westHam = new HashMap<String,List<Object>>();
	westHam.put(Division.ENGLISH_PREMIERSHIP.name(), teams);
    }

    @Test
    public void testDodgyInputNotEnough() {
	try {
	    when(request.getPathInfo()).thenReturn("");
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(HELP_TEXT));
	    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,HELP_TEXT);	    
	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}
    }

    @Test
    public void testDodgyInputTooMuch() {
	try {
	    when(request.getPathInfo()).thenReturn("/all/all/all");
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(HELP_TEXT));
	    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,HELP_TEXT);
	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}

    }

    @Test
    public void testDodgyInputNeitherAllNorHelp() {
	try {
	    when(request.getPathInfo()).thenReturn("/alles");
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(HELP_TEXT));
	    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,HELP_TEXT);
	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}

    }

    @Test
    public void testDodgyInputNoDivision() {
	try {
	    when(request.getPathInfo()).thenReturn("/division/");
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(HELP_TEXT));
	    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,HELP_TEXT);
	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}

    }

    @Test
    public void testGetHelp() {
	try {
	    when(request.getPathInfo()).thenReturn("/help");
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(HELP_TEXT));
	    verify(response,times(0)).sendError(anyInt(),anyString());

	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}

    }

    @Test
    public void testGetAllNoResults() {
	try {
	    when(request.getPathInfo()).thenReturn("/all");
	    when(api.getAllTeamsByDivision()).thenReturn(EMPTY_RESULTS);
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains("{}"));
	    verify(response,times(0)).sendError(anyInt(),anyString());

	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}
    }
    
    @Test
    public void testGetAllResultsExist() {
	try {
	    when(request.getPathInfo()).thenReturn("/all");
	    when(api.getAllTeamsByDivision()).thenReturn(westHam);
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(WHUFC));
	    verify(response,times(0)).sendError(anyInt(),anyString());

	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}	
    }

    @Test
    public void testGetByDivisionDodgyDivision() {
	try {
	    when(request.getPathInfo()).thenReturn("/division/woo");
	    when(api.getTeamsForDivision(Division.ENGLISH_PREMIERSHIP)).thenReturn(EMPTY_LIST);
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(HELP_TEXT));
	    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST,HELP_TEXT);
	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}	
    }
    
    
    @Test
    public void testGetByDivisionNoResultsExist() {
	try {
	    when(request.getPathInfo()).thenReturn("/division/ENGLISH_PREMIERSHIP");
	    when(api.getTeamsForDivision(Division.ENGLISH_PREMIERSHIP)).thenReturn(EMPTY_LIST);
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(NO_TEAMS));
	    verify(response,times(0)).sendError(anyInt(),anyString());

	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}	
    }
    
    @Test
    public void testGetByDivisionResultsExist() {
	try {
	    when(request.getPathInfo()).thenReturn("/division/ENGLISH_PREMIERSHIP");
	    when(api.getTeamsForDivision(Division.ENGLISH_PREMIERSHIP)).thenReturn(teams);
	    servlet.doGet(request, response);
	    assertTrue(writer.toString().contains(WHUFC));
	    verify(response,times(0)).sendError(anyInt(),anyString());

	}
	catch (Exception e) {
	    fail("Shouldn't have thrown exception "+e);
	}	
    }

}
