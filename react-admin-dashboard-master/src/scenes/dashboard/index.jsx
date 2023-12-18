
import { Box, Button, Typography, useTheme , Paper} from "@mui/material";
import axios from 'axios';
import { tokens } from "../../theme";
import { mockTransactions } from "../../data/mockData";
import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import EmailIcon from "@mui/icons-material/Email";
import React, { useState } from 'react';
import PointOfSaleIcon from "@mui/icons-material/PointOfSale";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import TrafficIcon from "@mui/icons-material/Traffic";
import { Create } from '../../Recipes/Create'; // Adjust the import path
import Header from "../../components/Header";
import LineChart from "../../components/LineChart";
import GeographyChart from "../../components/GeographyChart";
import BarChart from "../../components/BarChart";
import StatBox from "../../components/StatBox";
import ProgressCircle from "../../components/ProgressCircle";
import TextField from '@mui/material/TextField';
import Snackbar from "@mui/material/Snackbar";
import MuiAlert from "@mui/material/Alert";
import { styled } from "@mui/material/styles";
import { TableContainer, Table, TableHead, TableBody, TableRow, TableCell } from '@mui/material';



const Dashboard = () => {

 
  const theme = useTheme();
  const colors = tokens(theme.palette.mode);
  const [totalNutrients, setTotalNutrients] = useState({});
  const [recipeName, setRecipeName] = useState('');
  const [imagePreview, setImagePreview] = useState('');
  
  const [ingredients, setIngredients] = useState([]);
  const [ServingSize, setServingSize] = useState([1]);
  const [description, setdescription] = useState('');
  const [videoUrl, setVideoUrl] = useState('');
  const [shortUrlVideo, setshortUrlVideo] = useState('');

  const [promoCodes, setPromoCodes] = useState([]);
  const [newPromoImage, setNewPromoImage] = useState(null);
  const [newPromoCode, setNewPromoCode] = useState('');
  const [promoDescription, setPromoDescription] = useState('');
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState(""); 
  
  const [isVegetarian, setIsVegetarian] = useState(false);
  const [isNonVegetarian, setIsNonVegetarian] = useState(false);
  const [isVegan, setIsVegan] = useState(false);

  const tags = [];
  if (isVegetarian) tags.push('Vegetarian');
  if (isNonVegetarian) tags.push('Non-Vegetarian');
  if (isVegan) tags.push('Vegan');
  
  const [foodName, setFoodName] = useState('');
  const [foodList, setFoodList] = useState([]);


  // const API_KEY = 'CRWReWK8mthHhd7ph2r4lhrRFoSGwvTtQx1EVkc0'; // Replace with your actual API key

  // const [nutrients, setNutrients] = useState([]);
  const calculateTotalNutrients = () => {
    const totalNutrients = {};
  
    // Iterate through each ingredient in the list
    ingredients.forEach((ingredient) => {
      const nutrientInfo = ingredient.nutritionalInfo;
  
      // Iterate through each nutrient in the ingredient
      Object.entries(nutrientInfo).forEach(([nutrientID, value]) => {
        // If the nutrient exists in the totalNutrients object, add the value to it
        if (totalNutrients[nutrientID]) {
          totalNutrients[nutrientID] += value;
        } else {
          // If the nutrient doesn't exist, initialize it with the current value
          totalNutrients[nutrientID] = value;
        }
      });
    });
  
    // Return the accumulated total nutrients
    return totalNutrients;
  };

   // Function to update the total nutrients state
   const updateTotalNutrients = () => {
    const newTotalNutrients = calculateTotalNutrients();
    setTotalNutrients(newTotalNutrients);
  };

  // Call the updateTotalNutrients function whenever 'ingredients' change
  useState(() => {
    updateTotalNutrients();
  }, [ingredients]); // Depend
 

  const previewImage = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        setImagePreview(event.target.result);
      };
      reader.readAsDataURL(file);
    }
  };
  // const fetchNutritionInfo = (nixItemId) => {
  //   const apiUrl = `https://trackapi.nutritionix.com/v2/natural/nutrients?x=nix_item_id:${nixItemId}`;
  //   const headers = {
  //     'x-app-id': '6f7f165f',
  //     'x-app-key': '44168e217f2c7115fedb626c22d20529',
  //   };
  // }

  const API_BASE_URL ='https://trackapi.nutritionix.com/v2';
const APP_ID = '6f7f165f';
const APP_KEY = '44168e217f2c7115fedb626c22d20529'; // Ensure this key is accurate without extra characters

const [query, setQuery] = useState('');
const [searchResults, setSearchResults] = useState([]);
const [selectedFood, setSelectedFood] = useState(null);

const searchFoods = async (query) => {
  try {
    const response = await fetch(`${API_BASE_URL}/search/instant?query=${query}`, {
      method: 'GET',
      headers: {
        'x-app-id': APP_ID,
        'x-app-key': APP_KEY,
      },
    });
    const data = await response.json();
    setSearchResults(data.common);
    console.log('Food Search Results:', data.common);
  } catch (error) {
    console.error('Error searching foods:', error);
  }
};
const [nutritionalInfo, setNutritionalInfo] = useState(null);

const getNutritionalInfo = async (foodName, ServingSize) => {
  try {
    const response = await fetch(`${API_BASE_URL}/natural/nutrients`, {
      method: 'POST',
      headers: {
        'x-app-id': APP_ID,
        'x-app-key': APP_KEY,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ query: foodName }),
    });

    const data = await response.json();
    const nutrients = {};

    if (data.foods && data.foods.length > 0) {
      const nutrientsData = data.foods[0].full_nutrients;

      nutrientsData.forEach((nutrient) => {
        // Check if the nutrient ID is in the list of specific nutrients
        const specificNutrients = [
          208, 606, 605, 291, 269, 601, 307, 306, 301, 303, 318, 401, 324, 417, 418,
          304, 309, 323, 430, 404, 405, 406, 415, 417, 421, 431, 410, 305, 415, 317,
          312, 315, 314, 317, 418
          // Add the specific nutrient IDs here
        ];

        
        if (specificNutrients.includes(nutrient.attr_id)) {
          nutrients[nutrient.attr_id] = nutrient.value * ServingSize; // Multiply by serving size
        }
      });
    }

    setNutritionalInfo(nutrients);
  } catch (error) {
    console.error('Error fetching nutritional info:', error);
  }
};

  
const addFoodItem = (food) => {
  // Add the food item to the ingredients list
  setIngredients([...ingredients, food]);
};

const removeFoodItem = (index) => {
  // Remove the food item at the specified index from the ingredients list
  const updatedIngredients = [...ingredients];
  updatedIngredients.splice(index, 1);
  setIngredients(updatedIngredients);
};



const handleAddToIngredients = (food) => {
  addFoodItem(food);
};

const handleRemoveFromIngredients = (index) => {
  removeFoodItem(index);
};



  const handleVegetarianTag = () => {
    setIsVegetarian(!isVegetarian);
  };
  
  const handleNonVegetarianTag = () => {
    setIsNonVegetarian(!isNonVegetarian);
  };
  
  const handleVeganTag = () => {
    setIsVegan(!isVegan);
  };
  
  const handleServingSizeChange = (index, newSize) => {
    setServingSize((prevSizes) => {
      const updatedSizes = [...prevSizes];
      updatedSizes[index] = parseInt(newSize);
      return updatedSizes;
    });
  };
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    setNewPromoImage(file);
  };
  const handlePromoCodeChange = (e) => {
    setNewPromoCode(e.target.value);
  };
  const uploadPromoImage = () => {
    if (newPromoImage && newPromoCode) {
      const newPromo = {
        code: newPromoCode,
        image: newPromoImage,
      };
      setPromoCodes([...promoCodes, newPromo]);
      setNewPromoCode('');
      setNewPromoImage(null);
    }
  };
  const totalNutrientsArray = Object.entries(totalNutrients).map(([nutrientName, nutrientData]) => ({
    nutrientName,
    value: nutrientData.value,
    unitName: nutrientData.unitName,
  }));
  
  
  const handleFormSubmit = async (e) => {
    e.preventDefault();
    try {
      // Fetch nutrition information based on the provided food list
  
    
      const result = await Create.saveRecipe({
        recipeName,
        imagePreview,
        ingredients,// Update the ingredients with foodList
        servingSize: ServingSize,
        description,
        shortUrlVideo: shortUrlVideo, 
        videoUrl,
        tags: tags,
        
        // Pass other necessary data here
      });
  
      // Clear the form
      setRecipeName('');
      setImagePreview('');
      // ... (clear other fields)
  
      console.log('Recipe saved with objectId: ' + result.id);
      showSuccessMessage('Recipe successfully created');
    } catch (error) {
      console.error('Error saving Recipe:', error);
      showError(error.message);
    }
  };
  const showSuccessMessage = (message) => {
    setSuccessMessage(message);
  };
  const showError = (message) => {
    setErrorMessage(message);
  };
  const handleClose = (event, reason) => {
    if (reason === "clickaway") {
      return;
    }
    setSuccessMessage("");
    setErrorMessage("");
  };
  


  return (
    <Box m="20px">
      {/* HEADER */}
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Header title="DASHBOARD" subtitle="Welcome to your dashboard" />
        <Box>
          <Button
            sx={{
              backgroundColor: colors.blueAccent[700],
              color: colors.grey[100],
              fontSize: "14px",
              fontWeight: "bold",
              padding: "10px 20px",
            }}
          >
            <DownloadOutlinedIcon sx={{ mr: "10px" }} />
            Test
          </Button>
        </Box>
      </Box>
      {/* GRID & CHARTS */}
      <Box
        display="grid"
        gridTemplateColumns="repeat(12, 1fr)"
        gridAutoRows="140px"
        gap="20px"
      >
        {/* ROW 1 */}
        <Box
          gridColumn="span 3"
          backgroundColor={colors.primary[400]}
          display="flex"
          alignItems="center"
          justifyContent="center"
        >
          <StatBox
            title="12,361"
            subtitle="Total Recipes"
            progress="0.75"
            increase="+14%"
            icon={
              <EmailIcon
                sx={{ color: colors.greenAccent[600], fontSize: "26px" }}
              />
            }
          />
        </Box>
        <Box
          gridColumn="span 3"
          backgroundColor={colors.primary[400]}
          display="flex"
          alignItems="center"
          justifyContent="center"
        >
          <StatBox
            title="431,225"
            subtitle="Sales Obtained"
            progress="0.50"
            increase="+21%"
            icon={
              <PointOfSaleIcon
                sx={{ color: colors.greenAccent[600], fontSize: "26px" }}
              />
            }
          />
        </Box>
        <Box
          gridColumn="span 3"
          backgroundColor={colors.primary[400]}
          display="flex"
          alignItems="center"
          justifyContent="center"
        >
          <StatBox
            title="32,441"
            subtitle="New Clients"
            progress="0.30"
            increase="+5%"
            icon={
              <PersonAddIcon
                sx={{ color: colors.greenAccent[600], fontSize: "26px" }}
              />
            }
          />
        </Box>
        <Box
          gridColumn="span 3"
          backgroundColor={colors.primary[400]}
          display="flex"
          alignItems="center"
          justifyContent="center"
        >
          <StatBox
            title="1,325,134"
            subtitle="New Users"
            progress="0.80"
            increase="+43%"
            icon={
              <TrafficIcon
                sx={{ color: colors.greenAccent[600], fontSize: "26px" }}
              />
            }
          />
        </Box>
        
           
        <Box
        gridColumn="span 14"
  gridRow="span 6"
  backgroundColor={colors.primary[400]}
  overflow="auto"
>
  <Paper elevation={3} sx={{ padding: '30px', borderRadius: '16px', backgroundColor: colors.primary[400], textsize: "25px" }}>
  
  <Typography
  variant="h4"
  gutterBottom
  style={{
    borderBottom: '2px solid #4CAF50',
    paddingBottom: '10px',
    color: theme.palette.mode === "light" ? 'black' : 'white', // Change the text color to black in light mode
    fontWeight: 'bold',
    fontSize: "25px",
  }}
>
  Create Recipes
</Typography>
     <form id="recipeForm"  onSubmit={handleFormSubmit}>
      <div class="form-group" style={{ marginBottom: '25px' }}>
        <label for="recipeName">Recipe Name</label>
        <TextField
          type="text"
          id="recipeName"
          variant="outlined"
          fullWidth
          value={recipeName}
          onChange={(e) => setRecipeName(e.target.value)}
          p="15px"
        />
      </div>

      <div className="form-group" style={{ marginBottom: '20px', display: 'flex', flexDirection: 'column' }}>
  <label htmlFor="tags" style={{ marginBottom: '10px' }}>Recipe Tags</label>
  <div style={{ marginBottom: '10px' }}>
    <label>
      <input
        type="checkbox"
        checked={isVegetarian}
        onChange={handleVegetarianTag}
      /> Vegetarian
    </label>
  </div>
  <div style={{ marginBottom: '10px' }}>
    <label>
      <input
        type="checkbox"
        checked={isNonVegetarian}
        onChange={handleNonVegetarianTag}
      /> Non-Vegetarian
    </label>
  </div>
  <div style={{ marginBottom: '10px' }}>
    <label>
      <input
        type="checkbox"
        checked={isVegan}
        onChange={handleVeganTag}
      /> Vegan
    </label>
  </div>
</div>

<div>
  <TextField
    id="recipeImage"
    label="Recipe Image"
    type="file"
    variant="outlined"
    fullWidth
    InputLabelProps={{ shrink: true }}
    onChange={previewImage}
    sx={{ marginBottom: '20px' }}
  />
</div>
    
<div>
  {/* Search Box */}
  <input
    value={query}
    onChange={(e) => setQuery(e.target.value)}
    placeholder="Enter food item"
  />
  <button onClick={() => searchFoods(query)}>Search</button>

  <table>
  <thead>
    <tr>
      <th>Food Name</th>
      <th>Picture</th>
      <th>Nutritional Information</th>
      <th>Serving Quantity</th>
      <th>Serving Unit</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    {searchResults.map((food, index) => (
      <tr key={index}>
        <td>{food.food_name}</td>
        <td>
          <img src={food.photo.thumb} alt={food.food_name} width="80" height="80" />
        </td>
        <td>
        <button onClick={() => getNutritionalInfo(food.food_name, ServingSize[index])}>
  Get Nutritional Info
</button>
        </td>
        <td><input
  type="number"
  value={ServingSize[index] !== undefined ? ServingSize[index] : 1}
  onChange={(e) => handleServingSizeChange(index, e.target.value)}
/></td>
        <td>{food.serving_unit}</td>
        <td>
          <button onClick={() => handleAddToIngredients(food)}>Add</button>
        </td>
      </tr>
    ))}
  </tbody>
</table>
  {nutritionalInfo && (
  <div>
    <h2>Nutritional Information</h2>
    <table>
      <thead>
        <tr>
          <th>Nutrient</th>
          <th>Value</th>
          <th>Unit</th>
        </tr>
      </thead>
      <tbody>
        {Object.entries(nutritionalInfo).map(([attrId, value]) => {
          let nutrientName = '';
          let nutrientUnit = '';
          switch (parseInt(attrId)) {
            case 208:
              nutrientName = 'Fat';
              nutrientUnit = 'g';
              break;
            case 606:
              nutrientName = 'Saturated Fat';
              nutrientUnit = 'g';
              break;
            case 605:
              nutrientName = 'Trans Fat';
              nutrientUnit = 'g';
              break;
            case 291:
              nutrientName = 'Fibre';
              nutrientUnit = 'g';
              break;
            case 269:
              nutrientName = 'Sugar';
              nutrientUnit = 'g';
              break;
            case 601:
              nutrientName = 'Cholesterol';
              nutrientUnit = 'mg';
              break;
            case 307:
              nutrientName = 'Sodium';
              nutrientUnit = 'mg';
              break;
            case 306:
              nutrientName = 'Potassium';
              nutrientUnit = 'mg';
              break;
            case 301:
              nutrientName = 'Calcium';
              nutrientUnit = 'mg';
              break;
            case 303:
              nutrientName = 'Iron';
              nutrientUnit = 'mg';
              break;
            case 318:
              nutrientName = 'Vitamin A';
              nutrientUnit = 'mcg';
              break;
            case 401:
              nutrientName = 'Vitamin C';
              nutrientUnit = 'mg';
              break;
            case 324:
              nutrientName = 'Vitamin D';
              nutrientUnit = 'mcg';
              break;
            case 417:
              nutrientName = 'Folate';
              nutrientUnit = 'mcg';
              break;
            case 418:
              nutrientName = 'Vitamin B12';
              nutrientUnit = 'mcg';
              break;
            case 304:
              nutrientName = 'Thiamin/Vitamin B1';
              nutrientUnit = 'mg';
              break;
            case 309:
              nutrientName = 'Riboflavin/Vitamin B2';
              nutrientUnit = 'mg';
              break;
            case 323:
              nutrientName = 'Niacin';
              nutrientUnit = 'mg';
              break;
            case 430:
              nutrientName = 'Vitamin B6';
              nutrientUnit = 'mg';
              break;
            case 404:
              nutrientName = 'Choline';
              nutrientUnit = 'mg';
              break;
            case 405:
              nutrientName = 'Biotin';
              nutrientUnit = 'mcg';
              break;
            case 406:
              nutrientName = 'Pantothenate';
              nutrientUnit = 'mg';
              break;
            case 415:
              nutrientName = 'Phosphorus';
              nutrientUnit = 'mg';
              break;
            case 421:
              nutrientName = 'Iodine';
              nutrientUnit = 'mcg';
              break;
            case 431:
              nutrientName = 'Selenium';
              nutrientUnit = 'mcg';
              break;
            case 410:
              nutrientName = 'Copper';
              nutrientUnit = 'mg';
              break;
            case 305:
              nutrientName = 'Magnesium';
              nutrientUnit = 'mg';
              break;
            case 317:
              nutrientName = 'Manganese';
              nutrientUnit = 'mg';
              break;
            case 312:
              nutrientName = 'Vitamin E';
              nutrientUnit = 'mg';
              break;
            case 315:
              nutrientName = 'Vitamin K';
              nutrientUnit = 'mcg';
              break;
            case 314:
              nutrientName = 'Molybdenum';
              nutrientUnit = 'mcg';
              break;
            case 318:
              nutrientName = 'Chloride';
              nutrientUnit = 'mg';
              break;
            // Add more cases if necessary...
            default:
              break;
          
          }
          return (
            <tr key={attrId}>
              <td>{nutrientName}</td>
              <td>{value}</td>
              <td>{nutrientUnit}</td>
            </tr>
          );
        })}
      </tbody>
    </table>
  </div>


)}


  
  
  {/* Table to show added ingredients */}
<h2>Added Ingredients</h2>
<table>
  <thead>
    <tr>
      <th>Food Name</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    {/* Conditionally render added ingredients */}
    {ingredients.length > 0 && (
      <div>
        <ul>
          {ingredients.map((ingredient, index) => (
            <li key={index}>
              <ul style={{ listStyle: 'none', padding: 0 }}>
                <li style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
                  <span style={{ marginRight: '10px', fontSize: '16px' }}>{ingredient.food_name}</span>
                  <button onClick={() => removeFoodItem(index)}>Remove</button>
                </li>
              </ul>
            </li>
          ))}
        </ul>
      </div>
    )}
  </tbody>
</table>

<div>
      {/* Display total nutrients */}
      <h2>Total Nutrients</h2>
      <ul>
        {Object.entries(totalNutrients).map(([nutrientName, nutrientValue]) => (
          <li key={nutrientName}>
            {nutrientName}: {nutrientValue}
          </li>
        ))}
      </ul>
      {/* Other components and elements */}
    </div>

   
  </div>

  
  




      {/* Render Nutritional Information */}
      {/* Nutritional Information Table */}
      
    


{/* 
      {searchResults.length > 0 && (
        <div>
          <Typography variant="h5">Search Results</Typography>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Description</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {searchResults.map((food, index) => (
                  <TableRow key={index}>
                    <TableCell>{food.description}</TableCell>
                    <TableCell>
                      <Button
                        onClick={() => addFoodToYourList(food)}
                        variant="outlined"
                        color="primary"
                      >
                        Add
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      )} */}




      {/* {nutrients.length > 0 && (
        <div>
          <Typography variant="h5">Nutrients</Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Nutrient Name</TableCell>
                  <TableCell>Value</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {nutrients.map((nutrient, index) => (
                  <TableRow key={index}>
                    <TableCell>{nutrient.nutrientName}</TableCell>
                    <TableCell>{nutrient.value}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer> */}
      
      {/* )} */}
      {/* </div> */}
        <br></br>
        <div class="form-group" style={{ marginBottom: '20px' }}>
        <label for="servingSize">Serving Size</label>
        <TextField
          type="number"
          id="servingSize"
          variant="outlined"
          fullWidth
          value={ServingSize}
          onChange={(e) => setServingSize(e.target.value)}
        />
      </div>
    



      <div class="form-group" style={{ marginBottom: '20px' }}>
        <label for="description">description</label>
        <TextField
          type = "text"
          id="description"
          variant="outlined"
          fullWidth
          multiline
          rows={4}
          value={ description}
          onChange={(e) => setdescription(e.target.value)}
        />
      </div>
      <div class="form-group" style={{ marginBottom: '20px' }}>
        <label for="videoUrl">Video URL (Optional)</label>
        <TextField
          id="videoUrl"
          variant="outlined"
          fullWidth
          value={videoUrl}
          onChange={(e) => setVideoUrl(e.target.value)}
        />
      </div>
      
      <div class="form-group" style={{ marginBottom: '20px' }}>
        <label for="shortUrlVideo">Short Video</label>
        <TextField
          id="shortUrlVideo"
          variant="outlined"
          fullWidth
          value={shortUrlVideo}
          onChange={(e) => setshortUrlVideo(e.target.value)}
        />
        </div>
      
      <Button
        type="submit"
        variant="contained"
        color="primary"
        sx={{
          width: '100%',
          backgroundColor: '#4CAF50',
          '&:hover': { backgroundColor: '#45A744' },
        }}
      >
        Create Recipe
      </Button>
      <Box m="20px">
       {/* ... your existing code ... */}
    <Snackbar open={successMessage || errorMessage} autoHideDuration={6000} onClose={handleClose}>
      <MuiAlert
        elevation={6}
        variant="filled"
        onClose={handleClose}
        severity={successMessage ? "success" : "error"}
      >
        {successMessage || errorMessage}
      </MuiAlert>
    </Snackbar>
  </Box>
    </form>
  </Paper>
</Box>
  
    


{/* <Box
          gridColumn="span 6"
          gridRow="span 5"
          backgroundColor={colors.primary[400]}
          p="30px"
        > */}
          
  {/* <Paper elevation={3} sx={{ padding: '30px', borderRadius: '16px', backgroundColor: colors.primary[400], textsize: "25px" }}> */}
  
  {/* <Typography
  variant="h4"
  gutterBottom
  style={{
    borderBottom: '2px solid #4CAF50',
    paddingBottom: '10px',
    color: theme.palette.mode === "light" ? 'black' : 'white', // Change the text color to black in light mode
    fontWeight: 'bold',
    fontSize: "25px"
  }}
>
  Promo Information
</Typography>
                <form id="promoForm">
                  <div className="form-group" style={{ marginBottom: '25px' }}>
              <label for="promocode">Enter Promo code : </label>
                
              <TextField
               id="promoForm"
               variant="outlined"
               fullWidth
               value={recipeName}
               onChange={(e) => setPromoCodes(e.target.value)}
               p="15px"
              />
              
                  </div>
                 
               <div>
                <TextField
                id="PromoImage"
                label="Promo Image"
                type="file"
                variant="outlined"
                fullWidth
                InputLabelProps={{ shrink: true }}
                onChange={previewImage}
                sx={{ marginBottom: '20px' }}
               />
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={uploadPromoImage}
                      style={{
                        width: '100%',
                        backgroundColor: '#4CAF50',
                        '&:hover': { backgroundColor: '#45A744' },
                      }}
                    >
                      Add Promo Code
                    </Button>
                  </div>
                </form>
              </Paper>
            </Box>
      
  
        {/* ROW 2
       
        <Box
          gridColumn="span 8"
          gridRow="span 8"
          backgroundColor={colors.primary[400]}
          overflow="auto"
        >
          
          <Box
            display="flex"
            justifyContent="space-between"
            alignItems="center"
            borderBottom={`4px solid ${colors.primary[500]}`}
            colors={colors.grey[100]}
            p="15px"
          >
            <Typography color={colors.grey[100]} variant="h5" fontWeight="600">
              Recent Transactions
            </Typography>
          </Box>
          {mockTransactions.map((transaction, i) => (
            <Box
              key={`${transaction.txId}-${i}`}
              display="flex"
              justifyContent="space-between"
              alignItems="center"
              borderBottom={`4px solid ${colors.primary[500]}`}
              p="15px"
            >
              <Box>
                <Typography
                  color={colors.greenAccent[500]}
                  variant="h5"
                  fontWeight="600"
                >
                  {transaction.txId}
                </Typography>
                <Typography color={colors.grey[100]}>
                  {transaction.user}
                </Typography>
              </Box>
              <Box color={colors.grey[100]}>{transaction.date}</Box>
              <Box
                backgroundColor={colors.greenAccent[500]}
                p="5px 10px"
                borderRadius="4px"
              >
                ${transaction.cost}
              </Box>
            </Box>
          ))}
        </Box> */}
        
        {/* ROW 3 */}
        {/* <Box
          gridColumn="span 4"
          gridRow="span 2"
          backgroundColor={colors.primary[400]}
          p="30px"
        >
          <Typography variant="h5" fontWeight="600">
            Campaign
          </Typography>
          <Box
            display="flex"
            flexDirection="column"
            alignItems="center"
            mt="25px"
          >
            <ProgressCircle size="125" />
            <Typography
              variant="h5"
              color={colors.greenAccent[500]}
              sx={{ mt: "15px" }}
            >
              $48,352 revenue generated
            </Typography>
            <Typography>Includes extra misc expenditures and costs</Typography>
          </Box>
        // </Box> */} 


{/* 
<Box
gridColumn="span 4"
gridRow="span 2"
backgroundColor={colors.primary[400]}
p="30px"
>



<div class="form-group" style={{ marginBottom: '20px' }}>
    <label for="ingredient">Ingredient</label>
    <TextField
      type="text"
      id="ingredient"
      variant="outlined"
      fullWidth
      value={ingredient}
      onChange={(e) => setIngredient(e.target.value)}
    />
    <button onClick={searchIngredient}>Search Ingredient</button>
  </div>
  {ingredientInfo && (
    <div>
      <h2>Nutritional Information for {ingredient}:</h2>
      <p>Calories: {ingredientInfo.nf_calories} kcal</p>
      <p>Protein: {ingredientInfo.nf_protein} g</p>
      {/* Add more nutritional information fields here */}
    {/* </div> */}
  {/* )} */}
        {/* <Box
          gridColumn="span 4"
          gridRow="span 2"
          backgroundColor={colors.primary[400]}
        >
          <Typography
            variant="h5"
            fontWeight="600"
            sx={{ padding: "30px 30px 0 30px" }}
          >
            {/* Sales Quantity
          </Typography>
          <Box height="250px" mt="-20px">
            <BarChart isDashboard={true} />
          </Box> */}
        </Box> 
        {/* <Box
          gridColumn="span 4"
          gridRow="span 2"
          backgroundColor={colors.primary[400]}
          padding="30px"
        >
          <Typography
            variant="h5"
            fontWeight="600"
            sx={{ marginBottom: "15px" }}
          >
            Geography Based Traffic
          </Typography>
          <Box height="200px">
            <GeographyChart isDashboard={true} />
          </Box>
        </Box> */}
      </Box> 
     
  );
};


export default Dashboard;
