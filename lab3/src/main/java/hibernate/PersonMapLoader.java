package hibernate;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;

import java.util.*;

import data.Person;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

/**
 * Hibernate based MapLoader *
 */
public class PersonMapLoader implements MapLoader<Long, Person>, MapLoaderLifecycleSupport {

    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    Session session = sessionFactory.openSession();
    Query allKeysQuery = session.createQuery("select id from Person").setFetchSize(1000);

    public PersonMapLoader() {
    }

    @Override
    public void init(HazelcastInstance hz, Properties props, String arg2) {
    }

    @Override
    public Person load(Long key) {
        return (Person) session.get(Person.class, key);
    }

    @Override
    public Map<Long, Person> loadAll(Collection<Long> keys) {
        Criteria criteria = session.createCriteria(Person.class).add(Restrictions.in("id", keys));
        List<Person> persons = criteria.list();

        Map<Long, Person> map = new HashMap<Long, Person>();
        for (Person person : persons) {
            map.put(person.id, person);
        }
        return map;
    }

    @Override
    public Set<Long> loadAllKeys() {
        // For smaller result set sizes use: allKeysQuery.list()
        return (Set<Long>) new QueryIterable<Long>(allKeysQuery);
    }

    @Override
    public void destroy() {
        session.close();
    }

}
