// import { Box, Typography, useTheme } from "@mui/material";
// import { DataGrid } from "@mui/x-data-grid";
// import { tokens } from "../../theme";
// import { mockDataInvoices } from "../../data/mockData";
// import Header from "../../components/Header";

// const Invoices = () => {
//   const theme = useTheme();
//   const colors = tokens(theme.palette.mode);
//   const columns = [
//     { field: "id", headerName: "ID" },
//     {
//       field: "name",
//       headerName: "Name",
//       flex: 1,
//       cellClassName: "name-column--cell",
//     },
//     {
//       field: "phone",
//       headerName: "Phone Number",
//       flex: 1,
//     },
//     {
//       field: "email",
//       headerName: "Email",
//       flex: 1,
//     },
//     {
//       field: "cost",
//       headerName: "Cost",
//       flex: 1,
//       renderCell: (params) => (
//         <Typography color={colors.greenAccent[500]}>
//           ${params.row.cost}
//         </Typography>
//       ),
//     },
//     {
//       field: "date",
//       headerName: "Date",
//       flex: 1,
//     },
//   ];

//   return (
//     <Box m="20px">
//       <Header title="INVOICES" subtitle="List of Invoice Balances" />
//       <Box
//         m="40px 0 0 0"
//         height="75vh"
//         sx={{
//           "& .MuiDataGrid-root": {
//             border: "none",
//           },
//           "& .MuiDataGrid-cell": {
//             borderBottom: "none",
//           },
//           "& .name-column--cell": {
//             color: colors.greenAccent[300],
//           },
//           "& .MuiDataGrid-columnHeaders": {
//             backgroundColor: colors.blueAccent[700],
//             borderBottom: "none",
//           },
//           "& .MuiDataGrid-virtualScroller": {
//             backgroundColor: colors.primary[400],
//           },
//           "& .MuiDataGrid-footerContainer": {
//             borderTop: "none",
//             backgroundColor: colors.blueAccent[700],
//           },
//           "& .MuiCheckbox-root": {
//             color: `${colors.greenAccent[200]} !important`,
//           },
//         }}
//       >
//         <DataGrid checkboxSelection rows={mockDataInvoices} columns={columns} />
//       </Box>
//     </Box>
//   );
// };

// export default Invoices;
// import React, { useState, useEffect } from 'react';
// import { Create } from '../../Recipes/Create'; // Import the Create class
// import Parse from 'parse';
// import {
//   Box,
//   Typography,
//   Table,
//   TableBody,
//   TableCell,
//   TableContainer,
//   TableHead,
//   TableRow,
//   Paper,
//   Button,
// } from '@mui/material';

// function RecipeManagement() {
//   const [recipes, setRecipes] = useState([]);
//   const [selectedRecipe, setSelectedRecipe] = useState(null);

//   useEffect(() => {
//     // Fetch recipes from the database when the component mounts
//     async function fetchRecipes() {
//       try {
//         const Recipes = Parse.Object.extend('Recipes');
//         const query = new Parse.Query(Recipes);
//         const results = await query.find();
//         const recipesData = results.map((recipe) => ({
//           id: recipe.id,
//           ...recipe.attributes,
//         }));
//         setRecipes(recipesData);
//       } catch (error) {
//         console.error('Error fetching recipes:', error);
//       }
//     }

//     fetchRecipes();
//   }, []);

//   const handleEdit = (recipe) => {
//     setSelectedRecipe(recipe);
//   };

//   const handleUpdate = async (updatedRecipe) => {
//     try {
//       // Update the recipe in the database using the Create class
//       await Create.updateRecipe(updatedRecipe);
//       // Refresh the list of recipes
//       const updatedRecipes = recipes.map((recipe) =>
//         recipe.id === updatedRecipe.id ? updatedRecipe : recipe
//       );
//       setRecipes(updatedRecipes);
//       setSelectedRecipe(null);
//     } catch (error) {
//       console.error('Error updating recipe:', error);
//     }
//   };

//   const handleDelete = async (recipe) => {
//     try {
//       // Delete the recipe in the database using the Create class
//       await Create.deleteRecipe(recipe.id);
//       // Remove the deleted recipe from the list
//       const filteredRecipes = recipes.filter((r) => r.id !== recipe.id);
//       setRecipes(filteredRecipes);
//     } catch (error) {
//       console.error('Error deleting recipe:', error);
//     }
//   };

//   return (
//     <div>
//       <Typography variant="h4" gutterBottom>
//         Recipe Management
//       </Typography>
//       <TableContainer component={Paper}>
//         <Table>
//           <TableHead>
//             <TableRow>
//               <TableCell>Name</TableCell>
//               <TableCell>Ingredients</TableCell>
//               <TableCell>Description</TableCell>
//               <TableCell>Field1</TableCell>
//               <TableCell>Field2</TableCell>
//               <TableCell>Field3</TableCell>
//               <TableCell>Actions</TableCell>
//             </TableRow>
//           </TableHead>
//           <TableBody>
//             {recipes.map((recipe) => (
//               <TableRow key={recipe.id}>
//                 <TableCell>{recipe.name}</TableCell>
//                 <TableCell>{recipe.ingredients}</TableCell>
//                 <TableCell>{recipe.description}</TableCell>
//                 <TableCell>{recipe.field1}</TableCell>
//                 <TableCell>{recipe.field2}</TableCell>
//                 <TableCell>{recipe.field3}</TableCell>
//                 <TableCell>
//                   <Button variant="outlined" onClick={() => handleEdit(recipe)}>
//                     Edit
//                   </Button>
//                   <Button
//                     variant="outlined"
//                     color="error"
//                     onClick={() => handleDelete(recipe)}
//                   >
//                     Delete
//                   </Button>
//                 </TableCell>
//               </TableRow>
//             ))}
//           </TableBody>
//         </Table>
//       </TableContainer>
//       {selectedRecipe && (
//         <div>
//           <Typography variant="h5" gutterBottom>
//             Edit Recipe
//           </Typography>
//           <Button
//             variant="contained"
//             color="primary"
//             onClick={() => handleUpdate(selectedRecipe)}
//           >
//             Update
//           </Button>
//         </div>
//       )}
import Parse from 'parse';
import React, { useEffect, useState } from 'react';
import { Box, Button, Snackbar } from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import Header from '../../components/Header';
import MuiAlert from "@mui/material/Alert";

const RecipeManagement = () => {
  const [recipes, setRecipes] = useState([]);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);
  const [deletedRecipe, setDeletedRecipe] = useState(null);

  useEffect(() => {
    fetchRecipes();
  }, []);

  const fetchRecipes = async () => {
    try {
      const Recipe = Parse.Object.extend('Recipes');
      const query = new Parse.Query(Recipe);
      const results = await query.find();
      const recipesData = results.map((result) => ({
        ...result.attributes,
        id: result.id,
      }));
      setRecipes(recipesData);
    } catch (error) {
      console.error('Error fetching recipes:', error);
      showError("Error fetching recipes");
    }
  };
  const handleEdit = (recipe) => {
    setSelectedRecipe({ ...recipe });
  };
  const handleUpdate = async () => {
    try {
      if (selectedRecipe) {
        const Recipe = Parse.Object.extend('Recipes');
        const query = new Parse.Query(Recipe);
        const originalRecipe = await query.get(selectedRecipe.id);
  
        // Update the fields of the original recipe with the selectedRecipe data
        originalRecipe.set('name', selectedRecipe.name);
        originalRecipe.set('description', selectedRecipe.description);
  
        // Check if selectedRecipe.ingredients exists before updating
        if (selectedRecipe.ingredients) {
          // Check if ingredients exist before splitting
          const ingredientsArray = Array.isArray(selectedRecipe.ingredients) ? selectedRecipe.ingredients : selectedRecipe.ingredients.split(',');
          originalRecipe.set('ingredients', ingredientsArray);
        }
  
        await originalRecipe.save();
  
        // Find the index of the selectedRecipe in the recipes array
        const updatedRecipeIndex = recipes.findIndex(recipe => recipe.id === selectedRecipe.id);
  
        if (updatedRecipeIndex !== -1) {
          // Create a new array with the updated recipe at the same index
          const updatedRecipes = [...recipes];
          updatedRecipes[updatedRecipeIndex] = selectedRecipe;
  
          setRecipes(updatedRecipes);
          showSuccessMessage("Recipe successfully updated");
          setSelectedRecipe(null); // Reset the selectedRecipe
        }
      }
    } catch (error) {
      console.error('Error updating recipe:', error);
      showError(error.message);
    }
  };
  

  const handleDelete = (recipe) => {
    setDeletedRecipe(recipe);

    try {
      const Recipe = Parse.Object.extend('Recipes');
      const query = new Parse.Query(Recipe);
      query.get(recipe.id).then((object) => {
        object.destroy().then(() => {
          fetchRecipes();
          showSuccessMessage("Recipe successfully deleted");
        }).catch((error) => {
          console.error('Error deleting Recipe:', error);
          showError("Error deleting Recipe");
        });
      });
    } catch (error) {
      console.error('Error deleting Recipe:', error);
      showError("Error deleting Recipe");
    }
  };

  const handleUndoDelete = () => {
    if (deletedRecipe) {
      try {
        const Recipe = Parse.Object.extend('Recipes');
        const newRecipe = new Recipe();

        newRecipe.set('name', deletedRecipe.name);
        newRecipe.set('ingredients', deletedRecipe.ingredients);
        newRecipe.set('description', deletedRecipe.description);

        // Set the remaining fields with your desired default values
        newRecipe.set('likeCount', 0);
        newRecipe.set('createdAt', new Date());
        // Add other fields here and set default values

        newRecipe.save().then(() => {
          fetchRecipes();
          showSuccessMessage("Recipe successfully undeleted");
        }).catch((error) => {
          console.error('Error undeleting Recipe:', error);
          showError("Error undeleting Recipe");
        });

        setDeletedRecipe(null);
      } catch (error) {
        console.error('Error creating a new Recipe object:', error);
        showError("Error creating a new Recipe object");
      }
    }
  };

  const handleCancel = () => {
    setSelectedRecipe(null);
  };

  const showSuccessMessage = (message) => {
    setSuccessMessage(message);
    setErrorMessage(null);
  };

  const showError = (message) => {
    setErrorMessage(message);
    setSuccessMessage(null);
  };

  const handleClose = () => {
    setSuccessMessage(null);
    setErrorMessage(null);
  };

  const columns = [
    { field: 'id', headerName: 'ID', width: 75 },
    { field: 'name', headerName: 'Name', flex: 1, editable: true },
    { field: 'ingredients', headerName: 'Ingredients', flex: 1, editable: true },
    { field: 'description', headerName: 'Description', flex: 1, editable: true },
    { field: 'likeCount', headerName: 'Likes', flex: 1 },
    {
      field: 'createdAt',
      headerName: 'Created At',
      flex: 1,
    },
    {
      field: 'action',
      headerName: 'Action',
      flex: 1,
      renderCell: (params) => (
        <div>
          {selectedRecipe && selectedRecipe.id === params.row.id ? (
            <>
              <Button
                variant="outlined"
                color="secondary"
                onClick={handleUpdate}
              >
                Save
              </Button>
              <Button
                variant="outlined"
                color="secondary"
                onClick={handleCancel}
              >
                Cancel
              </Button>
            </>
          ) :(
            <>
             <Button
             variant="outlined"
             color="secondary"
             onClick={handleUpdate} // Make sure this line is like this
             >
            Update
            </Button>
              <Button
                variant="outlined"
                color="secondary"
                onClick={() => handleDelete(params.row)}
              >
                Delete
              </Button>
              <Button
                variant="outlined"
                color="secondary"
                onClick={() => handleEdit(params.row)}
              >
                Edit
              </Button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <Box m="20px">
      <Header title="Recipes" subtitle="Manage your recipes" />
      <Box m="40px 0 0 0" height="75vh">
        <DataGrid
          rows={recipes}
          columns={columns}
          pageSize={10}
          rowHeight={40}
          autoHeight
        />
      </Box>
      <Button variant="contained" color="primary" onClick={handleUndoDelete}>
        Undo Delete
      </Button>
      <Snackbar
        open={!!successMessage || !!errorMessage}
        autoHideDuration={6000}
        onClose={handleClose}
      >
        <MuiAlert
          elevation={6}
          variant="filled"
          onClose={handleClose}
          severity={successMessage ? 'success' : 'error'}
        >
          {successMessage || errorMessage}
        </MuiAlert>
      </Snackbar>
    </Box>
  );
};

export default RecipeManagement;
