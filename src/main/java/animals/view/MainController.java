package animals.view;

import animals.domain.Animal;
import animals.domain.Group;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import animals.services.AnimalRepository;
import animals.services.GroupRepository;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Controller
public class MainController
{
    @Autowired
    GroupRepository groupRepository;

    @Autowired
    AnimalRepository animalRepository;

    @GetMapping("/AnimalShelter")
    public String start (Model model)
    {
        List<Group> groups = groupRepository.findAll();
        model.addAttribute("groups", groups);
        //System.out.println(groups);

        Displayed displayed = new Displayed();
        displayed.setNothing();
        model.addAttribute("displayed", displayed);

        //System.out.println(System.getProperty("user.dir"));

        return "main";
    }

    @PostMapping("/AnimalShelter")
    public String postAnimalExcel (@RequestParam("excel") MultipartFile excel)
    {
        try (InputStream inputStream = excel.getInputStream())
        {
            Workbook wb = WorkbookFactory.create(inputStream);
            Sheet sheet = wb.getSheetAt(0);

            int curRow = 1;
            while (true)
            {
                Row row = sheet.getRow(curRow++);
                String groupName = row.getCell(0).getStringCellValue();
                String name = row.getCell(1).getStringCellValue();

                if (groupName.equals("") || name.equals("")) break;

                String description = "";
                if (row.getCell(2) != null) {
                    description = row.getCell(2).getStringCellValue();
                }
                
                NewAnimal newAnimal = new NewAnimal(groupName, name, description);
                addAnimal(newAnimal);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored){}

        return "redirect:/AnimalShelter";
    }

    @GetMapping("/AnimalShelter/newAnimal")
    public String getNewAnimal (Model model)
    {
        List<Group> groups = groupRepository.findAll();
        model.addAttribute("groups", groups);

        Displayed displayed = new Displayed();
        displayed.setAddAnimal();
        model.addAttribute("displayed", displayed);

        model.addAttribute("newAnimal", new NewAnimal());

        return "main";
    }

    @PostMapping("/AnimalShelter/newAnimal")
    public String postNewAnimal (Model model, NewAnimal newAnimal)
    {
        model.addAttribute("newAnimal", newAnimal);

        Long idAnimal = addAnimal(newAnimal);

        if (idAnimal == null) return "redirect:/AnimalShelter/newAnimal";

        return "redirect:/AnimalShelter/animal?id=" + idAnimal;
    }

    @GetMapping("/AnimalShelter/animal")
    public String getAnimal (@RequestParam(value = "id") String id, Model model)
    {
        List<Group> groups = groupRepository.findAll();
        model.addAttribute("groups", groups);

        Displayed displayed = new Displayed();
        displayed.setAnimal();
        model.addAttribute("displayed", displayed);

        Animal animal = animalRepository.getOne(Long.parseLong(id));
        model.addAttribute("animal", animal);

        model.addAttribute("image", null);

        return "main";
    }

    @PostMapping("/AnimalShelter/animal")
    public String postAnimal (
            @RequestParam(value = "id") String id,
            @RequestParam("image") MultipartFile image,
            Model model,
            Animal changedAnimal
    ) throws IOException {
        /*List<Group> groups = groupRepository.findAll();
        model.addAttribute("groups", groups);*/

        model.addAttribute("animal", changedAnimal);

        Animal animal = animalRepository.getOne(Long.parseLong(id));
        animal.setName(changedAnimal.getName());
        animal.setDescription(changedAnimal.getDescription());
        //animal.setImagePath(changedAnimal.getImagePath());

        if (!image.isEmpty())
        {
            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = "images\\"+uuidFile+"."+image.getOriginalFilename();

            image.transferTo(new File(System.getProperty("user.dir")+"\\"+resultFilename));

            animal.setImagePath(resultFilename);
        }

        animalRepository.save(animal);

        return "redirect:/AnimalShelter/animal?id=" + id;
    }

    @RequestMapping("/AnimalShelter/delete")
    public String deleteAnimal (@RequestParam(value = "id") String id)
    {
        Animal animal = animalRepository.getOne(Long.parseLong(id));
        Group group = animal.getGroup();
        group.deleteAnimal(animal);
        groupRepository.save(group);

        /*
        if (group.getAnimals().isEmpty())
        {
            groupRepository.delete(group);
        }
        */

        return "redirect:/AnimalShelter";
    }

    @RequestMapping("/AnimalShelter/deleteGroup")
    public String deleteGroup (@RequestParam(value = "id") String id)
    {
        Group group = groupRepository.getOne(Long.parseLong(id));
        groupRepository.delete(group);

        return "redirect:/AnimalShelter";
    }

    @RequestMapping("/AnimalShelter/exportToPDF")
    public String exportToPDF (@RequestParam(value = "id") String id, HttpServletResponse response)
            throws IOException, DocumentException
    {
        Animal animal = animalRepository.getOne(Long.parseLong(id));

        //инициализация документа
        Document document = new Document();
        String docURI = "exportPDF\\"+animal.getId()+"."+animal.getName()+".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(docURI));

        //натройка базового шрифта на кириллице
        final String FONT = "assets\\fonts\\arial.ttf";
        BaseFont baseFont = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        //открыть
        document.open();

        //заголовок
        Font font = new Font(baseFont, 20, Font.NORMAL);
        Paragraph paragraph = new Paragraph("В приюте пополнение - "+animal.getName()+"\n", font);
        document.add(paragraph);

        //изображение
        try {
            Image image = Image.getInstance(animal.getImagePath());
            float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0)
                    / image.getWidth()) * 100;
            image.scalePercent(scaler);
            document.add(image);
        }
        catch (NullPointerException | FileNotFoundException ignored){}

        //описание
        if (animal.getDescription() != null) {
            font = new Font(baseFont, 14, Font.NORMAL);
            paragraph = new Paragraph("\n\n" + animal.getDescription(), font);
            document.add(paragraph);
        }

        //дата записи
        font = new Font(baseFont, 10, Font.NORMAL);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        paragraph = new Paragraph("\n\nПринят в приюте "+dateFormat.format(animal.getDate()), font);
        document.add(paragraph);

        //сохранить
        document.close();

        //открыть в браузере
        File file = new File(docURI);
        byte[] bytes = new byte[(int) file.length()];
        try(FileInputStream fis = new FileInputStream(file)){
            fis.read(bytes);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Type", "application/pdf");
        response.addHeader("Content-Disposition", "attachment; filename=" + file.getName());
        OutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();

        return null;
    }

    public Long addAnimal(NewAnimal newAnimal)
    {
        if (newAnimal.getGroup().equals("") || newAnimal.getName().equals(""))
            return null;

        List<Group> groups = groupRepository.findAll();

        for (Group group : groups)
        {
            if (group.getName().equalsIgnoreCase(newAnimal.getGroup()))
            {
                newAnimal.setGroup(group.getName());
                break;
            }
        }

        Group group = groupRepository.findByName(newAnimal.getGroup());

        if (group == null)
        {
            group = new Group(newAnimal.getGroup());
            groupRepository.save(group);
        }

        Animal animal = new Animal(group, newAnimal.getName(), new Date());
        animal.setDescription(newAnimal.getDescription());

        animalRepository.save(animal);
        groupRepository.save(group);

        return animal.getId();
    }

}

//вспомогательный класс для отображения необходимой информации
class Displayed
{
    private boolean nothing;
    private boolean animal;
    private boolean addAnimal;

    Displayed()
    {
        this.nothing = false;
        this.animal = false;
        this.addAnimal = false;
    }

    public void setNothing()
    {
        this.nothing = true;
        this.animal = false;
        this.addAnimal = false;
    }

    public void setAnimal()
    {
        this.nothing = false;
        this.animal = true;
        this.addAnimal = false;
    }

    public void setAddAnimal()
    {
        this.nothing = false;
        this.animal = false;
        this.addAnimal = true;
    }

    public boolean isNothing()
    {
        return nothing;
    }

    public boolean isAnimal()
    {
        return animal;
    }

    public boolean isAddAnimal()
    {
        return addAnimal;
    }
}

//вспомогательный класс для внесения новой записи
class NewAnimal
{
    private String group;
    private String name;
    private String description;

    NewAnimal() {};

    public NewAnimal(String group, String name, String description)
    {
        this.group = group;
        this.name = name;
        this.description = description;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }
}
