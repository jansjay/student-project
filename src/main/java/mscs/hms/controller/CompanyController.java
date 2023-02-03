package mscs.hms.controller;

import mscs.hms.model.Company;
import mscs.hms.service.AddressService;
import mscs.hms.service.CompanyService;
import mscs.hms.service.IUserService;
import java.util.Dictionary;
import java.util.Hashtable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CompanyController extends AbsEntityController<Company> {
    
    @Autowired
    private CompanyService companyService;

    @Autowired
    private IUserService userService;

    @Autowired
    private AddressService addressService;

    @GetMapping("/companies")
    public ModelAndView showCompanies(Model model) {
        LOG.info("In companies view");
        return getListEntitiesModelView(companyService.findAll());
    }    

    @GetMapping("/company_new")
    public ModelAndView newCompanyForm() {
        LOG.info("In companies new");
        ModelAndView modelAndView = getEditViewModel(new Company(), "new");
        return modelAndView;
    }    

    @GetMapping("/company_edit/{id}")
    public ModelAndView editCompanyForm(@PathVariable(value="id") final Integer companyId) {
        LOG.info("In companies edit");
        return getEditViewModel(companyService.get(companyId), "edit");        
    }

    @PostMapping("/company/delete") 
    public ModelAndView requestOTP( @RequestParam(value="id") Integer id) {
        LOG.info("In companies delete");
        companyService.delete(id);
        return getListEntitiesModelView(companyService.findAll());
    }

    @PostMapping("/company/edit")
    public ModelAndView processEdit(Company company) {
        LOG.info("In companies edit");
        companyService.save(company);
        return getListEntitiesModelView(companyService.findAll());
    }

    @PostMapping("/company/new")
    public ModelAndView processNew(Company company) {
        LOG.info("In companies new");
        companyService.save(company);
        return getListEntitiesModelView(companyService.findAll());
    } 
    
    @Override
    public Class<?> getClassType(){
        return Company.class;
    }
    @Override
    public String getEditViewPath(){
        return "/company_edit";
    }
    @Override
    public String getListViewPath(){
        return "/companies";
    }
    @Override
    public String getNewViewPath(){
        return "/company_new";
    }
    @Override
    public String getCrudPath(){
        return "/company";
    }
    @Override
    public Dictionary<String, Iterable<?>> getSelectLists(){
        Dictionary<String, Iterable<?>> dictionary = new Hashtable<>();
        //Note used same attributeName "systemUser"
        dictionary.put("systemUser", userService.findAllUsers());
        dictionary.put("address", addressService.findAll());
        return dictionary;
    }
}
