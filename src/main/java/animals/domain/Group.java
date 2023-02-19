package animals.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "group_t")
public class Group implements Comparable<Group>
{
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    //название группы
    private String name;

    //животные в группе
    @JsonIgnore
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Animal> animals = new TreeSet<>();


    public Group() { }

    public Group(String name)
    {
        this.name = name;
    }



    public Long getId()
    {
        return id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setAnimals(TreeSet<Animal> animals)
    {
        this.animals = animals;
    }

    public TreeSet<Animal> getAnimals()
    {
        return new TreeSet<>(this.animals);
    }

    public void deleteAnimal(Animal animal)
    {
        animals.remove(animal);
    }


    @Override
    public int compareTo(Group group)
    {
        int comp = name.compareTo(group.getName());
        if (comp == 0) {
            return id.compareTo(group.getId());
        }
        return comp;
    }
}
