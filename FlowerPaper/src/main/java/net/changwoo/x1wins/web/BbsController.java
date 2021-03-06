package net.changwoo.x1wins.web;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import net.changwoo.x1wins.entity.Bbs;
import net.changwoo.x1wins.entity.Reply;
import net.changwoo.x1wins.entity.Response;
import net.changwoo.x1wins.service.BbsService;
import net.changwoo.x1wins.service.FileService;
import net.changwoo.x1wins.service.ReplyService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(value = "/bbs")
public class BbsController {
     
    private static final Logger logger = LoggerFactory.getLogger(BbsController.class);
	private static int perPage = 10;
	private static String errorPage = "error.tiles";
	private static String menu = "bbs";
	private static String classname = "bbs";
     
    @Autowired
    private BbsService bbsService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ReplyService replyService;

    @RequestMapping(value = "/{bbsnum}/form", method = RequestMethod.GET)
    public String showForm(@PathVariable("bbsnum") int bbsnum, Map model, HttpServletRequest request) {
    	
    	model.put("menu", menu);
		try {
			
			Bbs bbs = new Bbs();
			model.put("bbs", bbs);
			model.put("bbsnum", bbsnum);
			model.put("menu", menu);
			
			bbsService.validWrite(bbsnum, request);
			
		} catch (Exception e) {
			logger.debug(e.toString());
			model.put("message", e.toString());
			return errorPage;
		}
    	
    	
    	
    	return "bbs/form.tiles";
    }
    
    @RequestMapping(value = "/{bbsnum}/form", method = RequestMethod.POST)
	public String processForm(@PathVariable("bbsnum") int bbsnum, @Valid Bbs bbs, BindingResult result,
			Map model, @RequestParam("file") MultipartFile mFiles[], HttpServletRequest request) {

    	model.put("menu", menu);
		try {
			
			logger.info(bbs.getClass()+"result.getAllErrors()"+result.getAllErrors());
			if (result.hasErrors()) {
				return "bbs/form.tiles";
			}
			
			bbsService.validWrite(bbsnum, request);
			
//			bbs.setBbsnum(bbsnum);
			bbsService.saveBbs(bbs, bbsnum, request);
			int snum = bbs.getNum();
			fileService.saveFiles(mFiles, snum, classname, request);

		} catch (Exception e) {
			logger.debug(e.toString());
			model.put("message", e.toString());
			return errorPage;
		}
		
		return "redirect:/bbs/"+bbsnum+"/list/1";
	}
    
    @RequestMapping(value = "/{bbsnum}/updateform/{num}", method = RequestMethod.GET)
    public String showUpdateForm(@PathVariable("bbsnum") int bbsnum, @PathVariable("num") int num, Locale locale, Map model, HttpServletRequest request) {
    	
    	model.put("menu", menu);
    	Bbs bbs;
		try {
			bbsService.validOwn(num, request);
			bbs = bbsService.findDetail(num);
			model.put("bbs", bbs);
			
			int snum = num;
			List fileList = fileService.findFileList(classname, bbsnum, snum, request);
    		model.put("filelist", fileList);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug(e.toString());
			model.put("message", e.toString());
			return errorPage;
		}
    	
		model.put("bbsnum", bbsnum);
    	return "bbs/updateform.tiles";
    }
    
    @RequestMapping(value = "/{bbsnum}/updateform/{num}", method = RequestMethod.POST)
	public String processUpdateForm(@PathVariable("bbsnum") int bbsnum, @PathVariable("num") int num, @Valid Bbs bbs, BindingResult result,
			Map model, @RequestParam("file") MultipartFile mFiles[], HttpServletRequest request) {

    	model.put("menu", menu);
    	
		try {
			
			if (result.hasErrors()) {
				return "bbs/updateform.tiles";
			}
			
			bbsService.validOwn(num, request);
			logger.info(bbs.getClass()+"result.getAllErrors()"+result.getAllErrors());
			
			bbsService.updateBbs(bbs, bbsnum, request);
			int snum = bbs.getNum();
			fileService.saveFiles(mFiles, snum, classname, request);

		} catch (Exception e) {
			logger.debug(e.toString());
			model.put("message", e.toString());
			return errorPage;
		}
		
		return "redirect:/bbs/"+bbsnum+"/list/1";
	}
    
    @RequestMapping(value = "/{bbsnum}/delete/{num}", method = RequestMethod.GET)
    public String delete(@PathVariable("bbsnum") int bbsnum, @PathVariable("num") int num, Locale locale, Map model, HttpServletRequest request, HttpServletResponse response) {
    	
    	model.put("menu", menu);
    	
    	try {
    		
    		bbsService.validOwn(num, request);
    		bbsService.delete(bbsnum, num);
//    		int snum = bbs.getNum();
//			fileService.saveFiles(mFiles, snum, classname);
//    		response.sendRedirect(request.getContextPath()+"/index");
    		
    	} catch (Exception e) {
    		logger.debug(e.toString());
    		model.put("message", e.toString());
    		return errorPage;
    	}
    	
    	return "redirect:/bbs/"+bbsnum+"/list/1";
    }
    
    
    @RequestMapping(value = "/{bbsnum}/list/{pagenum}", method = RequestMethod.GET)
    public String showBbsList(@PathVariable("bbsnum") int bbsnum, @PathVariable("pagenum") int pageNum, Locale locale, Map model, HttpServletRequest request) {
    	
    	model.put("menu", menu);
		try {
			
			if (bbsService.validRead(bbsnum, request)) {
				bbsService.findListAndPaging(bbsnum,pageNum, perPage , model, request);
			}
//			else{
//				throw new Exception("Exception ------ can't read bbs list");
//			}
			
		} catch (Exception e) {
			logger.debug(e.toString());
			model.put("message", e.toString());
			return errorPage;
		}
		
    	
        return "bbs/list.tiles";
    }

    @RequestMapping(value = "/data/{bbsnum}/list/{pagenum}.{type}", method = RequestMethod.GET)
	public ModelAndView showBbsListData(@PathVariable("bbsnum") int bbsnum,
			@PathVariable("pagenum") int pageNum,
			@PathVariable("type") String type, Locale locale,
			HttpServletRequest request) {
    	
    	Map resultMap = new HashMap();
    	Response response = new Response();
		try {
			
//			HttpSession session = request.getSession(false);
//			if(session == null) {
//				
//				response.setStatus("FAIL");
//				response.setResult(resultMap);
//			} else {

//				String userid = session.getAttribute("userid").toString();
//				logger.info("session userid "+userid);
//				logger.info("session getid() "+session.getId());
				
				bbsService.findListAndPaging(bbsnum,pageNum, perPage , resultMap, request);
				response.setStatus("SUCCESS");
				response.setResult(resultMap);
//			}
			
			
			
		} catch (Exception e) {
			logger.debug(e.toString());
			response.setStatus("FAIL");
		}
    	
		ModelAndView modelAndView = getModelAndView(response, type);
		return modelAndView;
    }
    
    @RequestMapping(value = "/{bbsnum}/detail/{num}", method = RequestMethod.GET)
    public String showBbsDetail(@PathVariable("bbsnum") int bbsnum, @PathVariable("num") int num, Locale locale, Map model, HttpServletRequest request) {
    	
    	model.put("menu", menu);
    	Bbs detail = null;
    	try {
    		
    		bbsService.validRead(bbsnum, request);
    		
    		detail = bbsService.findDetail(num);
    		int snum = num;
    		List fileList = fileService.findFileList(classname, bbsnum, snum, request);
    		model.put("detail", detail);
    		model.put("bbsnum", bbsnum);
    		model.put("filelist", fileList);
    		
    		Reply reply = new Reply();
			model.put("reply", reply);
    		
    	} catch (Exception e) {
    		logger.debug(e.toString());
    		model.put("message", e.toString());
    		return errorPage;
    	}
    	
    	
    	return "bbs/detail.tiles";
    }
    
	@RequestMapping(value = "/data/detail/{num}.{type}", method = RequestMethod.GET)
	public ModelAndView showBbsDetailData(@PathVariable("num") int num,
			@PathVariable("type") String type) {

		Bbs bbs = null;
		Map resultMap = new HashMap();
		try {
			bbs = bbsService.findDetail(num);
			resultMap.put("bbs", bbs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug(e.toString());
		}
		
		ModelAndView modelAndView = getModelAndView(resultMap, type);
		return modelAndView;
	}
    
    @RequestMapping(value = "/detail/{num}/reply/add", method = RequestMethod.POST)
	public @ResponseBody String addReply(@PathVariable("num") int num, @Valid Reply reply, BindingResult result, Map model, HttpServletRequest request) {

    	String status ="";
    	model.put("menu", menu);
		try {
			
			logger.info(reply.getClass()+"result.getAllErrors()"+result.getAllErrors());
			Response response = new Response();
			if (result.hasErrors()) {
				status = "FAIL";
				response.setResult(result);
			}else{
				status = "SUCCESS";
				replyService.saveReply(reply, num, request);
				response.setResult(reply);
			}
			response.setStatus(status);
			

		} catch (Exception e) {
			logger.debug(e.toString());
			model.put("message", e.toString());
			status = e.toString();
//			return errorPage;
		}
		
		return status;
	}
    
    //http://www.raistudies.com/spring/spring-mvc/ajax-form-validation-using-spring-mvc-and-jquery/
//    @RequestMapping(value="/AddUser.htm",method=RequestMethod.POST)
//    public @ResponseBody JsonResponse addUser(@ModelAttribute(value="user") User user, BindingResult result ){
//            JsonResponse res = new JsonResponse();
//            ValidationUtils.rejectIfEmpty(result, "name", "Name can not be empty.");
//            ValidationUtils.rejectIfEmpty(result, "education", "Educatioan not be empty");
//            if(!result.hasErrors()){
//                    userList.add(user);
//                    res.setStatus("SUCCESS");
//                    res.setResult(userList);
//            }else{
//                    res.setStatus("FAIL");
//                    res.setResult(result.getAllErrors());
//            }
//
//            return res;
//    }
    
    @RequestMapping(value = "/data/{bbsnum}/reply/list.{type}", method = RequestMethod.GET)
    public ModelAndView showReplyData(@PathVariable("bbsnum") int bbsnum, @PathVariable("type") String type) {

    	List<Reply> list = null;
		try {
			list = replyService.findReplyList(bbsnum);
			logger.debug("size : "+list.size());
    		logger.debug("list : "+list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug(e.toString());
		}
		
//		JSONArray jsonArray = JSONArray.fromObject(list);
//		
//		JSONObject result = new JSONObject();
//		result.put("list", jsonArray);
		
		ModelAndView modelAndView = getModelAndView(list, type);
		return modelAndView;
//		HttpHeaders responseHeaders = new HttpHeaders();
//		responseHeaders.add("Content-Type", "text/html; charset=UTF-8");
//		return new ResponseEntity<String>(JSONObject.fromObject(result)
//				.toString(), responseHeaders, HttpStatus.CREATED);
	}
    
    @RequestMapping(value = "/test", method = RequestMethod.GET)
	public @ResponseBody
	ResponseEntity<String> TEST() {
		JSONArray jsonArray = new JSONArray();
		jsonArray.add("1");
		jsonArray.add("2");
		for (Object name : jsonArray) {
			System.out.println(name + "������123");
		}
		JSONObject result = new JSONObject();
		result.put("list", jsonArray);

		Map<String, String> map = new HashMap<String, String>();
		map.put("firstname", "��");
		map.put("secondname", "��������");
		result.put("map", map);

		// return JSONObject.fromObject(result).toString();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=UTF-8");
		return new ResponseEntity<String>(JSONObject.fromObject(result)
				.toString(), responseHeaders, HttpStatus.CREATED);

	}
	
	@RequestMapping(value = "/test2/{num}.{type}", method = RequestMethod.GET)
	public ModelAndView TEST2(@PathVariable("num") int num, @PathVariable("type") String type) {

		Bbs bbs = null;
		try {
			bbs = bbsService.findDetail(num);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug(e.toString());
		}
		
		Map resultMap = new HashMap();
	    resultMap.put("bbs", bbs);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addAllObjects(resultMap);
		modelAndView.setViewName(type+"View");
		return modelAndView;

		// return "jsonView";
	}
	
	/**
	 * 
	 * @param resultMap
	 * @param type
	 * @return ModelAndView
	 * @throws Exception
	 */
	public ModelAndView getModelAndView(Map resultMap, String type)
	{
		ModelAndView modelAndView = new ModelAndView();
		
		try{
			modelAndView.addAllObjects(resultMap);
			
			if(!type.equals("json")&&!type.equals("xml")){
				type = "json";
			}
			
			modelAndView.setViewName(type + "View");
			
		}catch(Exception e){
			logger.debug(e.toString());
		}
		return modelAndView;
	}
	public ModelAndView getModelAndView(Object attributeValue, String type)
	{
		ModelAndView modelAndView = new ModelAndView();
		
		try{
			modelAndView.addObject(attributeValue);
			
			if(!type.equals("json")&&!type.equals("xml")){
				type = "json";
			}
			
			modelAndView.setViewName(type + "View");
			
		}catch(Exception e){
			logger.debug(e.toString());
		}
		return modelAndView;
	}
}