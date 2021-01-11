package springboard.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import model.JdbcTemplateConst;
import springboard.model.JDBCTemplateDAO;
import springboard.model.SpringBbsDTO;
import sprngboard.command.BbsCommandImpl;
import sprngboard.command.DeleteActionCommand;
import sprngboard.command.EditActionCommand;
import sprngboard.command.EditCommand;
import sprngboard.command.ListCommand;
import sprngboard.command.ViewCommand;
import sprngboard.command.WriteActionCommand;

@Controller
public class BbsController {
	
	/*
	@Autowired
		: 스프링 설정파일에서 이미 생성된 빈을 자동으로 주입받고 싶을때 사용.
		타입을 기반으로 자동주입되며, 만약 해당 타입의 빈이 존재하지 않으면
		에러발생되어 서버를 시작할 수 없음.
		-생성자, 멤버변수, 메소드(setter)에 적용가능함
		-타입을 이용해 자동으로 프로퍼티 값을 설정함.
		-해당 어노테이션은 멤버변수에만 적용할 수 있다. 함수내의
		지역변수에서는 사용할 수 없다.
		-타입을 통해 자동으로 설정되므로 같은 타입이 2개이상 존재하면 예외발생됨.
	 */
	private JdbcTemplate template;
	
	@Autowired
	public void setTemplate(JdbcTemplate template) {
		this.template = template;
		System.out.println("@Autowired=>JdbcTemplate 연결성공");
		//JdbcTemplate을 해당 프로그램 전체에서 사용하기 위한 설정
		JdbcTemplateConst.template = this.template;
	}
	
	/*
	멤버변수이므로 클래스에서 전역적으로 사용할수 있다. 해당 클래스의
	모든 command객체는 해당 인터페이스를 구현하여 정의한다.
	 */
	BbsCommandImpl command = null;
	
	//게시판 리스트
	@RequestMapping("/board/list.do")
	public String list(Model model, HttpServletRequest req) {
		/*
		사용자로부터 받은 모든 요청은 request내장객체에 저장되고, 이를 커맨드객체로
		전달하기 위해 model에 저장한 후 매개변수로 전달한다.
		 */
		model.addAttribute("req", req);
		command = new ListCommand();
		command.execute(model);
		
		return "07Board/list";
	}
	
	//글쓰기 - 폼에 진입시에는 Get방식으로 처리
	@RequestMapping("/board/write.do")
	public String write(Model model) {
		return "07Board/write";
	}
	
	//글쓰기 - 폼값을 전송하므로 post방식으로 처리되므로 value/method로 기술
	@RequestMapping(value="/board/writeAction.do", method=RequestMethod.POST)
	public String writeAction(Model model, HttpServletRequest req, SpringBbsDTO springBbsDTO) {
		//request객체를 모델객체에 저장 
		model.addAttribute("req", req);
		//View에서 전송한 폼값을 커맨드객체를 통해 저장후 model에 저장
		model.addAttribute("springBbsDTO", springBbsDTO);
		command = new WriteActionCommand();
		command.execute(model);
		
		//새로운 페이지에 대한 요청을 한다. 즉 페이지 이동이된다.
		return "redirect:list.do?nowPage=1";
	}
	
	//게시물 상세보기
	@RequestMapping("/board/view.do")
	public String view(Model model, HttpServletRequest req) {
		model.addAttribute("req", req);
		command = new ViewCommand();
		command.execute(model);
		
		return "07Board/view";
	}
	
	@RequestMapping("/board/password.do")
	public String password(Model model, HttpServletRequest req) {
		model.addAttribute("idx", req.getParameter("idx"));
		return "07Board/password";
	}
	
	
	@RequestMapping("/board/passwordAction.do")
	public String passwordAction(Model model, HttpServletRequest req) {
		String modePage = null;
		
		String mode = req.getParameter("mode");
		String idx = req.getParameter("idx");
		String nowPage = req.getParameter("nowPage");
		String pass = req.getParameter("pass");
		
		JDBCTemplateDAO dao = new JDBCTemplateDAO();
		
		int rowExist = dao.password(idx,pass);
		if(rowExist<=0) {
			model.addAttribute("isCorrMsg", "패스워드가 일치하지 않습니다.");
			model.addAttribute("idx", idx);
			
			modePage = "07Board/password";
			
		}
		else {
			System.out.println("검증완료");
		}
		
		//얘를 여기다가 입력하는게 맞나???★★★★ 수정하기 작동 안한다규..
		if(mode.equals("edit")) {
			//수정이면 수정폼으로 이동한다.
			model.addAttribute("req",req);
			command = new EditCommand();
			command.execute(model);
			
			modePage = "07Board/edit";
		}
		else if(mode.equals("delete")) {
			model.addAttribute("req", req);
			command = new DeleteActionCommand();
			command.execute(model);
			
			model.addAttribute("nowPage", req.getParameter("nowPage"));
			modePage = "redirect:list.do";
		}
		
		return modePage;
	}
	
	@RequestMapping("/board/editAction.do")
	public String editAction(HttpServletRequest req,
			Model model, SpringBbsDTO springBbsDTO) {
		
		model.addAttribute("req", req);
		model.addAttribute("springBbsDTO", springBbsDTO);
		command = new EditActionCommand();
		command.execute(model);
		
		/*
		뷰로 이동시 redirect를 이용하면 해당 페이지로 리다이렉트(이동)된다.
		이때 파라미터가 필요하다면 model객체에 저장하기만 하면 자동으로
		쿼리스트링으로 추가된다.
		 */
		model.addAttribute("idx", req.getParameter("idx"));
		model.addAttribute("nowPage", req.getParameter("nowPage"));
		
		return "redirect:view.do";
	}
	
	
}