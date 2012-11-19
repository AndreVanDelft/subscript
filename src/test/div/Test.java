/*
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private Password password;
    
    public User(String firstName, String lastName, 
                 String email, Password password) 
    {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.password  = password;
    }
    
    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) {this.firstName = firstName;}

    public String getLastName() {return lastName;}
    public void setLastName(String lastName) {this.lastName = lastName;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public Password getPassword() {return password;}
    public void setPassword(Password password) {this.password = password;}

    @Override
    public String toString() {
            return "User [email=" + email + ", firstName=" + firstName
                            + ", lastName=" + lastName + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime*result + ((email    ==null)? 0 : email    .hashCode());
      result = prime*result + ((firstName==null)? 0 : firstName.hashCode());
      result = prime*result + ((lastName ==null)? 0 : firstName.hashCode());
      result = prime*result + ((password ==null)? 0 : password .hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            User other = (User) obj;
            if (email == null) {
            	if (other.email != null) return false;
            } else if (!email.equals(other.email)) return false;
            if (password == null) {
                    if (other.password != null) return false;
            } else if (!password.equals(other.password))
                    return false;
            if (firstName == null) {
                    if (other.firstName != null)  return false;
            } else if (!firstName.equals(other.firstName))
                    return false;
            if (lastName == null) {
                    if (other.lastName != null) return false;       
            } else if (!lastName.equals(other.lastName)) return false;
            return true;
    }               
}
*/