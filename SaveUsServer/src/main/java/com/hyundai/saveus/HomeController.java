package com.hyundai.saveus;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

/**
 * Handles requests for the application home page.
 */

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@RequestMapping(value = "/insert.do", method = RequestMethod.POST)
	public String insert(HttpServletRequest request2,MultipartHttpServletRequest request, ModelMap model)
			throws IllegalStateException, IOException {
		String address  = request.getParameter("address");
		Map<String, MultipartFile> files = request.getFileMap();
		CommonsMultipartFile cmf = (CommonsMultipartFile) files.get("photo");
		// 경로
		String pdfPath = request.getSession().getServletContext().getRealPath("/resources");
		System.out.println("pdf : " + pdfPath);
		String savePath = ""+pdfPath+"/common/img/"+cmf.getOriginalFilename();
		System.out.println("저장 경로 : " +savePath);

		File file = new File(savePath);
		// 파일 업로드 처리 완료.
		cmf.transferTo(file);

		try {
			// insert method
			model.addAttribute("result", "업로드 성공");
		} catch (Exception e) {
			model.addAttribute("result", "업로드 실패");
		}
		
		
		return "fileupload";
	}

	@RequestMapping(value = "/home.do", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);

		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

		String formattedDate = dateFormat.format(date);

		model.addAttribute("serverTime", formattedDate);

		return "Hello";
	}

	@RequestMapping(value = "/receiver.do")
	public String test(HttpServletRequest request, Model model) throws Exception {
		request.setCharacterEncoding("utf-8");
		String id = request.getParameter("id");
		URLEncoder.encode(id, "euc-kr");
		String passwd = request.getParameter("pw");
		System.out.println("id : " + id + " pwd : " + passwd);
		model.addAttribute("id", id);
		return "home";
	}
	void parseXml(String address) {
		
	}
	@RequestMapping(value = "/dataInsert.do")
	public void test(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		request.setCharacterEncoding("utf-8");
		String address = request.getParameter("address");
		String address_arr = "";
		String tel_arr = "";
		try {
			Document document =DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("http://openapi.seoul.go.kr:8088/43546a415776697334324a6d5a4847/xml/SebcPoliceStationKor/1/1000");
			Element root = document.getDocumentElement();
			NodeList address_list = root.getElementsByTagName("ADD_KOR_ROAD");
			NodeList n_list = root.getElementsByTagName("H_KOR_GU");
			NodeList tel = root.getElementsByTagName("TEL");
			
			for (int i = 0; i < n_list.getLength(); i++) {
				if (n_list.item(i).getFirstChild().getTextContent().equals(address)) {
					address_arr += address_list.item(i).getFirstChild().getTextContent() + "/";
					tel_arr += tel.item(i).getFirstChild().getTextContent() + "/";
				}
			}
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(address_arr +" /  " + tel_arr);
		sendPush(address_arr, tel_arr);
		
	}
	
	@RequestMapping(value = "/push.do")
	public void msgSend(HttpServletRequest request) throws Exception {
//		sendPush();
	}
	
	public void sendPush(String address, String tel) {
		String msg = "움직임 포착!!";
		String register_id="APA91bGGbLhsjufEDdz90wulEfrb6AQGvanOqHzzRyp8q8gbgRt0-N2Ju48RGorQv3H5GB-WcAeUKd4-tuujRsp7LgANzoM59etxz6tJv7Lj0WqUmbDDU_7r5MEy3tP1pIzTTg1UQZ9s";
		try {
			
			msg = URLEncoder.encode(msg, "euc-kr");
			address = URLEncoder.encode(address, "euc-kr");
			tel = URLEncoder.encode(tel, "euc-kr");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String simpleApiKey = "AIzaSyBcjjjjShUZgjaLk2aq2Cbc4oqE_nWMiFw";
		ArrayList<String> regid = new ArrayList<String>(); // reg_id

		String MESSAGE_ID = String.valueOf(Math.random() % 100 + 1);

		boolean SHOW_ON_IDLE = false;

		int LIVE_TIME = 1 ; 

		int RETRY = 2;
		regid.add(register_id);
		Sender sender = new Sender(simpleApiKey);
		Message message = new Message.Builder()

				.collapseKey(MESSAGE_ID)

				.delayWhileIdle(SHOW_ON_IDLE)

				.timeToLive(LIVE_TIME).addData("msg", msg).addData("address", address).addData("tel", tel)

				.build();

		MulticastResult result1 = null;
		try {
			result1 = sender.send(message, regid, RETRY);
			System.out.println(result1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (result1 != null) {

			List<Result> resultList = result1.getResults();

			for (Result result : resultList) {

				System.out.println(result.getErrorCodeName());

			}

		}

	}

}
