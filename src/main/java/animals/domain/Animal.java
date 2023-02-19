package animals.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "animal_t")
public class Animal implements Comparable<Animal>
{
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    //кличка
    private String name;

    @Column (columnDefinition = "LONGTEXT")
    private String description;

    //фото
    private String imagePath;

    //дата поступления
    private Date date;

    //группа
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_t", nullable = false)
    private Group group;


    public Animal() { }

    public Animal(Group group, String name, Date date)
    {
        this.name = name;
        this.date = date;
        this.group = group;
    }


    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public Group getGroup()
    {
        return group;
    }

    public void setGroup(Group group)
    {
        this.group = group;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    @Override
    public int compareTo(Animal animal)
    {
        int comp = name.compareTo(animal.getName());
        if (comp == 0){
            comp = id.compareTo(animal.getId());
        }
        return comp;
    }
}
