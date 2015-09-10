package webapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {
	
	@Value("${pipes.db.password}")
	private String password;
	
//	@RequestMapping("/")
//	public String test() {
//		System.out.println("here");
//	    return "wew";
//	}
//	
//	@RequestMapping("/a")
//	public String test2() {
//		System.out.println("here2");
//	    return "redirect:/www.baidu.com";
//	}
//	
//	@RequestMapping("/b")
//	public String test3() {
//		System.out.println(password);
//		System.out.println("here3");
//	    return "www.baidu.com";
//	}
}
