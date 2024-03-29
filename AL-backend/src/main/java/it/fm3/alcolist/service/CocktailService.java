package it.fm3.alcolist.service;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.fm3.alcolist.DTO.CocktailDTO;
import it.fm3.alcolist.DTO.CocktailResultDTO;
import it.fm3.alcolist.DTO.IngredientDTO;
import it.fm3.alcolist.entity.Cocktail;
import it.fm3.alcolist.entity.Ingredient;
import it.fm3.alcolist.repository.CocktailRepository;
import it.fm3.alcolist.repository.IngredientRepository;

@Service
@Transactional
public class CocktailService implements CocktailServiceI{
	
	@Autowired
	private CocktailRepository cocktailRepository;
	
	@Autowired
	private IngredientServiceI ingredientService;
	@Autowired
	private IngredientRepository ingredientRepository;
	@Override
	public Cocktail add(CocktailDTO cocktailDto) throws Exception {
		Cocktail newCocktail= new Cocktail();
		cocktailDto.isAlcoholic = false;
		this.buildCocktailByCocktailDTO(newCocktail, cocktailDto);
		if(cocktailRepository.findByUuid(cocktailDto.uuid) != null)
			throw new Exception("Cocktail already exists");
		cocktailRepository.save(newCocktail);
		return newCocktail;
	}

	@Override
	public Cocktail delete(String uuid) throws Exception {
		Cocktail cocktailToDelete = get(uuid);
		cocktailRepository.delete(cocktailToDelete);
		return cocktailToDelete;
	}
	
	@Override
	public Cocktail update(CocktailDTO c) throws Exception {
		Cocktail cocktailToUpdate = cocktailRepository.findByUuid(c.uuid);
		if(cocktailToUpdate==null)
			throw new Exception("cocktail not found");
		this.buildCocktailByCocktailDTO(cocktailToUpdate, c);
		return cocktailToUpdate;
	}

	@Override
	public Cocktail get(String uuid) throws Exception {
		Cocktail c=cocktailRepository.findByUuid(uuid);
		if(c==null) throw new Exception ("Cocktail with uuid "+uuid+" not exists");
//		Set<Ingredient> ingredientsList = this.getIngredients(uuid);
//		System.out.println("###UEUEUEUEUEU SI BLOCCA QUA");
//		System.out.println("####INGREDIENTS " + ingredientsList);
		c.setIngredients(cocktailRepository.findByUuid(uuid).getIngredients());
		return c;
	}
	
	@Override
	public Set<Ingredient> getIngredients(String uuid) {
		 return cocktailRepository.findByUuid(uuid).getIngredients();
	}
	
	@Override
    public CocktailResultDTO getMenu(CocktailDTO cocktailDTO) throws Exception {
        cocktailDTO.inMenu = true;
        return searchByFields(cocktailDTO);
        //cocktailRepository.findByInMenu(pageable);
    }

    @Override
    public CocktailResultDTO getMenuIba(CocktailDTO cocktailDTO) throws Exception {
        cocktailDTO.isIBA = true;
        return searchByFields(cocktailDTO);
        //cocktailRepository.findByInMenu(pageable);
    }
	
	private void buildCocktailByCocktailDTO(Cocktail cocktail, CocktailDTO cocktailDTO) throws Exception {
		checkCocktailNameField(cocktailDTO.name);
		if(!StringUtils.hasText(cocktailDTO.name) || cocktailDTO.price == null || !StringUtils.hasText(cocktailDTO.description) || cocktailDTO.flavour == null || cocktailDTO.isIBA == null || cocktailDTO.inMenu == null || cocktailDTO.isAlcoholic == null || !StringUtils.hasText(cocktailDTO.pathFileImg))
			throw new Exception("Compile all fields");
		cocktail.setName(cocktailDTO.name);
		cocktail.setPrice(cocktailDTO.price);
		cocktail.setDescription(cocktailDTO.description);
		cocktail.setFlavour(cocktailDTO.flavour);
		cocktail.setIBA(cocktailDTO.isIBA);
		cocktail.setInMenu(cocktailDTO.inMenu);
		cocktail.setAlcoholic(cocktailDTO.isAlcoholic);
		cocktail.setPathFileImg(cocktailDTO.pathFileImg);
		if(!StringUtils.hasText(cocktailDTO.uuid))
			cocktail.setUuid(UUID.randomUUID().toString());
		else
			cocktail.setUuid(cocktailDTO.uuid);
	}
	
	@Override
	public CocktailResultDTO searchByFields(CocktailDTO cocktailDTO) throws Exception {
		CocktailResultDTO cocktailResult = new CocktailResultDTO();
		
		cocktailResult.cocktail = searchByFieldsRes(cocktailDTO , cocktailResult);
		
		if(cocktailResult.cocktail.size() == 0 ) {
			cocktailResult.totalResult=0;
			cocktailResult.itemsPerPage =0;
			cocktailResult.startIndex=0;
		}else {
			cocktailResult.itemsPerPage = cocktailResult.cocktail.size();
		}
		return cocktailResult;
	}
	
	private List<Cocktail>  searchByFieldsRes(CocktailDTO cocktailDTO,CocktailResultDTO cocktailResultDTO) throws Exception  {
		Pageable pageable = null;
		if(cocktailDTO.page != null && cocktailDTO.size != null) {
			pageable = PageRequest.of(cocktailDTO.page.intValue(), cocktailDTO.size.intValue());
			if(cocktailDTO.page.intValue() > 0)
				cocktailResultDTO.startIndex = cocktailDTO.page.intValue() * cocktailDTO.size.intValue() + 1;
			else
				cocktailResultDTO.startIndex = cocktailDTO.page.intValue() + 1;
		}
		return searchByFieldsSimple(cocktailDTO, pageable,cocktailResultDTO);
	}
	
	private List<Cocktail> searchByFieldsSimple(CocktailDTO cocktailDTO, Pageable pageable,CocktailResultDTO cocktailResultDTO) throws Exception {
		if((cocktailDTO.inMenu)!=null) {
			cocktailResultDTO.totalResult= cocktailRepository.countByInMenu(true);
			//if(pageable!=null)
				return cocktailRepository.findByInMenu(pageable, true);
			//else return cocktailRepository.findByInMenu(true);
		}
		if((cocktailDTO.isIBA)!=null) {
			cocktailResultDTO.totalResult= cocktailRepository.countByIsIBA(true);
			//if(pageable!=null)
				return cocktailRepository.findByIsIBA(pageable,true);
			//else return cocktailRepository.findByInMenu(true);
		}
		if(!StringUtils.hasText(cocktailDTO.name) && !StringUtils.hasText(cocktailDTO.flavour) && (cocktailDTO.isAlcoholic)==null) {
			//SE TUTTI VUOTI RICERCA TUTTO
			cocktailResultDTO.totalResult= cocktailRepository.count();
			if(pageable!=null)
				return cocktailRepository.findAll(pageable).getContent();
			else return cocktailRepository.findAll();
		}
		else if(StringUtils.hasText(cocktailDTO.name) && !StringUtils.hasText(cocktailDTO.flavour) && (cocktailDTO.isAlcoholic)==null) {
			//SOLO NOME
			if(!this.checkLengthName(cocktailDTO.name))
				return null;
			cocktailResultDTO.totalResult = cocktailRepository.countByNameContainingIgnoreCase(cocktailDTO.name);
			if(pageable!=null)
				return cocktailRepository.findByNameContainsIgnoreCase(pageable, cocktailDTO.name);
			else return cocktailRepository.findByNameContainsIgnoreCase(cocktailDTO.name);
		}	
		else if(!StringUtils.hasText(cocktailDTO.name) && StringUtils.hasText(cocktailDTO.flavour) && (cocktailDTO.isAlcoholic)==null) {
			//SOLO GUSTO
			cocktailResultDTO.totalResult = cocktailRepository.countByFlavourContainsIgnoreCase(cocktailDTO.flavour);
			if(pageable!=null)
				return cocktailRepository.findByFlavourContainsIgnoreCase(pageable, cocktailDTO.flavour);
			else return cocktailRepository.findByFlavourContainsIgnoreCase(cocktailDTO.flavour);
		}
		else if(!StringUtils.hasText(cocktailDTO.name) && !StringUtils.hasText(cocktailDTO.flavour) && (cocktailDTO.isAlcoholic)!=null) {
			//SOLO ALCOLICO
			cocktailResultDTO.totalResult = cocktailRepository.countByIsAlcoholic(cocktailDTO.isAlcoholic);
			if(pageable!=null)
				return cocktailRepository.findByIsAlcoholic(pageable, cocktailDTO.isAlcoholic);
			else return cocktailRepository.findByIsAlcoholic(cocktailDTO.isAlcoholic);
		}
		else if(StringUtils.hasText(cocktailDTO.name) && StringUtils.hasText(cocktailDTO.flavour) && (cocktailDTO.isAlcoholic)==null) {
			// NOME-GUSTO
			if(!this.checkLengthName(cocktailDTO.name))
				return null;
			cocktailResultDTO.totalResult = cocktailRepository.countByNameAndFlavourContainingIgnoreCase(cocktailDTO.name.toUpperCase(), cocktailDTO.flavour);
			if(pageable!=null)
				return cocktailRepository.findByNameAndFlavourContainsIgnoreCase(pageable, cocktailDTO.name.toUpperCase(), cocktailDTO.flavour);
			else return cocktailRepository.findByNameAndFlavourContainsIgnoreCase(cocktailDTO.name.toUpperCase(), cocktailDTO.flavour);
		}
		else if(StringUtils.hasText(cocktailDTO.name) && !StringUtils.hasText(cocktailDTO.flavour) && (cocktailDTO.isAlcoholic)!=null) {
			// NOME-ALCOLICO
			if(!this.checkLengthName(cocktailDTO.name))
				return null;
			cocktailResultDTO.totalResult = cocktailRepository.countByNameAndIsAlcoholic(cocktailDTO.name.toUpperCase(), cocktailDTO.isAlcoholic);
			if(pageable!=null)
				return cocktailRepository.findByNameAndIsAlcoholic(pageable, cocktailDTO.name.toUpperCase(), cocktailDTO.isAlcoholic);
			else return cocktailRepository.findByNameAndIsAlcoholic(cocktailDTO.name.toUpperCase(), cocktailDTO.isAlcoholic);
		}
		else if(!StringUtils.hasText(cocktailDTO.name) && StringUtils.hasText(cocktailDTO.flavour) && (cocktailDTO.isAlcoholic)!=null) {
			// GUSTO-ALCOLICO
			cocktailResultDTO.totalResult = cocktailRepository.countByFlavourAndIsAlcoholic(cocktailDTO.flavour, cocktailDTO.isAlcoholic);
			if(pageable!=null)
				return cocktailRepository.findByFlavourAndIsAlcoholic(pageable, cocktailDTO.flavour, cocktailDTO.isAlcoholic);
			else return cocktailRepository.findByFlavourAndIsAlcoholic(cocktailDTO.flavour, cocktailDTO.isAlcoholic);
		}
		else {
			//COMPLETA
			if(!this.checkLengthName(cocktailDTO.name))
				return null;
			cocktailResultDTO.totalResult = cocktailRepository.countByNameAndFlavourAndIsAlcoholic(cocktailDTO.name.toUpperCase(), cocktailDTO.flavour, cocktailDTO.isAlcoholic);
			if(pageable!=null)
				return cocktailRepository.findByNameAndFlavourAndIsAlcoholic(pageable, cocktailDTO.name.toUpperCase(), cocktailDTO.flavour, cocktailDTO.isAlcoholic);
			else return cocktailRepository.findByNameAndFlavourAndIsAlcoholic(cocktailDTO.name.toUpperCase(), cocktailDTO.flavour, cocktailDTO.isAlcoholic);
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public void updateSold(String uuid, int operation) throws Exception {
		Cocktail c = this.get(uuid);
		Integer actualSold = c.getSold();
		if(operation == this.INCREMENT) //INCREMENTO
			c.setSold(actualSold + 1);
		else //DECREMENTO
			c.setSold(actualSold - 1);	
	}	
	
	private boolean checkLengthName(String name) throws Exception {
		boolean check = true;
		if(this.checkIfStringContainsDigit(name)) {
			check = false;
			throw new Exception("Nome deve essere una stringa alfanumerica");
		}
		if(name.length() < 3) {
			check = false;
			throw new Exception("Nome deve essere una stringa di almeno 3 caratteri");
		}
		if(name.length() > 30) {
			check = false;
			throw new Exception("Nome deve essere una stringa di massimo 30 caratteri");
		}
		return check;
	}
	
	private boolean checkCocktailNameField(String name) throws Exception {
		boolean check = true;
		if(name.equalsIgnoreCase("") || name.isEmpty() || name == null)
			throw new Exception("Bisogna inserire il Nome del cocktail");
		if(name.length() < 3) {
			check = false;
			throw new Exception("Nome deve essere una stringa di almeno 3 caratteri");
		}
		if(name.length() > 30) {
			check = false;
			throw new Exception("Nome deve essere una stringa di massimo 30 caratteri");
		}
		return check;
	}
	
	public boolean checkIfStringContainsDigit(String passCode){
	      for (int i = 0; i < passCode.length(); i++) {
	        if(Character.isDigit(passCode.charAt(i))) {
	            return true;
	        }
	      }
	      return false;
	}
	
	public Ingredient deleteIngredient(IngredientDTO ingredientDTO) throws Exception {
		/* remove reference from course, else delete does nothing
	    Course c = getRegistration().getCourse();
	    c.getRegistrations().remove(getRegistration());
	    courseMgr.saveOrUpdate(c);

	    // delete registration from the database
	    registrationMgr.delete(reg);*/
		System.out.println("sono in deleteIngredient");
		Cocktail c= this.get(ingredientDTO.cocktailUuid);
		c.getIngredients().remove(ingredientService.get(ingredientDTO.uuid));
		cocktailRepository.save(c);
		Ingredient i=ingredientService.delete(ingredientDTO.uuid);
		
		return i;
	}
	
	public Ingredient addIngredient(IngredientDTO i) throws Exception {
		Cocktail c=get(i.cocktailUuid);
		Ingredient newIngredient=this.ingredientService.add(i);
		newIngredient.setCocktail(c);
		if(newIngredient.getProduct().getAlcoholicPercentage()>0)
			c.setAlcoholic(true);
		ingredientRepository.save(newIngredient);
		cocktailRepository.save(c);
		return newIngredient;
	}
	
	
}
