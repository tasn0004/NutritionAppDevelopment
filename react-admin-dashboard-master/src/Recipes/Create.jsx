// class Create {
//   static async saveRecipe({
//     recipeName,
//     imagePreview,
//     ingredients,
//     servingSize,
//     description,
//     videoUrl,
//     nutritionInformation,
//     timesFavourited,
//     timesLiked,
//     timeInMinutes,
//     tags,
//     numberOfComments,
//     shortUrlVideo,
//     background,
//   }) {
//     const apiUrl = 'https://food-nutrition.canada.ca/api/canadian-nutrient-file/';

//     const recipeData = {
//       name: recipeName || '',
//       imagePreview: imagePreview || '',
//       servingSize: parseInt(servingSize, 10) || 0,
//       description: description || '',
//       timesFavourited: timesFavourited || 0,
//       videoUrl: videoUrl || '',
//       timesLiked: timesLiked || 0,
//       timeInMinutes: timeInMinutes || 0,
//       tags,
//       numberOfComments: numberOfComments || 0,
//       shortUrlVideo,
//       background: background || 'Background Info',
//     };

//     // Checking if ingredients is an array and not empty before setting it
//     if (Array.isArray(ingredients) && ingredients.length > 0) {
//       recipeData.ingredients = ingredients;
//     } else {
//       console.error('Ingredients should be a non-empty array.');
//       return; // Handle the case where ingredients are invalid (optional, based on your use case)
//     }

//     // // Check if nutritionInformation is an object before setting it
//     // if (typeof nutritionInformation === 'object') {
//     //   recipeData.nutritionInformation = nutritionInformation;
//     // } else {
//     //   console.error('nutritionInformation should be an object.');
//     //   return; // Handle the case where nutritionInformation is invalid (optional, based on your use case)
//     // }

//     try {
//       const recipesResponse = await fetch(apiUrl + 'Recipes', {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//         },
//         body: JSON.stringify(recipeData),
//       });

//       if (recipesResponse.ok) {
//         const result = await recipesResponse.json();
//         console.log('Recipe saved with objectId: ' + result.id);
//         return result;
//       } else {
//         console.error('Error saving Recipe:', recipesResponse.statusText);
//         throw new Error('Error saving Recipe');
//       }
//     } catch (error) {
//       console.error('Error saving Recipe:', error.message);
//       throw error;
//     }
//   }
// }

// export { Create };
